package com.sitepark.ies.aggregator.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * The full navigation path from the outermost root resolver to a given resolver, spanning every
 * link/scope boundary crossed along the way.
 *
 * <h2>Scopes vs. the full path</h2>
 *
 * <p>{@link Resolver#root()} and {@link Resolver#globalRoot()} answer "where am I in the current /
 * outermost scope". A {@code ResolverPath} additionally records every step taken to get here. The
 * path itself never resets: {@link #segments()} keeps growing for the entire navigation chain, even
 * across scope boundaries that reset {@link #root()}.
 *
 * <h2>Creating resolvers along the path</h2>
 *
 * <p>A resolver's own path ends with a segment referring to that very resolver — a chicken-and-egg
 * situation, because the resolver needs its path at construction time. This is resolved with a
 * {@link Factory}: {@link #descend(String, Factory)}, {@link #enterScope(String, Factory)} and
 * {@link #createRoot(Factory)} build the (extended) path first, hand it to the factory to create
 * the resolver, and then bind the created resolver back into the path via a set-once holder. The
 * factory therefore receives a fully-built path and returns the resolver.
 *
 * <p><strong>Contract:</strong> a factory must only <em>store</em> the supplied path; it must not
 * query {@link #root()}, {@link #globalRoot()} or the segment resolvers while creating the resolver,
 * as those references are only bound once the factory returns. Doing so throws {@link
 * IllegalStateException}.
 *
 * <p>Equality and {@code toString} are intentionally identity-based for the resolver components, so
 * that {@code ResolverPath} never calls {@link Object#equals(Object)}, {@link Object#hashCode()} or
 * {@link Object#toString()} on a resolver (which could recurse back into its path).
 */
public final class ResolverPath {

  private final Supplier<Resolver> globalRoot;

  private final Supplier<Resolver> root;

  private final List<Segment> segments;

  private ResolverPath(
      Supplier<Resolver> globalRoot, Supplier<Resolver> root, List<Segment> segments) {
    this.globalRoot = globalRoot;
    this.root = root;
    this.segments = List.copyOf(segments);
    if (this.segments.isEmpty()) {
      throw new IllegalArgumentException("segments must not be empty");
    }
  }

  /**
   * Creates a factory that produces the child resolver for a given path.
   *
   * <p>The factory receives the fully-built path (including the segment for the resolver it is about
   * to create) and returns that resolver.
   */
  @FunctionalInterface
  public interface Factory {

    /**
     * Creates the resolver for the given path.
     *
     * @param path the resolver's own, fully-built navigation path; store it, do not query it
     * @return the created resolver
     */
    Resolver create(ResolverPath path);
  }

  /**
   * Creates the initial, single-segment path for an already-existing root resolver.
   *
   * <p>Use this when the root resolver already exists (for example a null-object). For a resolver
   * that must be created <em>with</em> its path, use {@link #createRoot(Factory)}.
   *
   * @param root the root resolver; must not be {@code null}
   * @return a path whose {@link #globalRoot()} and {@link #root()} are both {@code root}
   */
  public static ResolverPath of(Resolver root) {
    Objects.requireNonNull(root, "root must not be null");
    return new ResolverPath(() -> root, () -> root, List.of(new Segment(null, root)));
  }

  /**
   * Creates the root resolver of a fresh resolver tree via the given factory.
   *
   * <p>The created resolver becomes both {@link #root()} and {@link #globalRoot()} of its path.
   *
   * @param factory creates the root resolver from its path
   * @return the created root resolver
   */
  public static Resolver createRoot(Factory factory) {
    Objects.requireNonNull(factory, "factory must not be null");
    Latch self = new Latch();
    ResolverPath path = new ResolverPath(self, self, List.of(new Segment(null, self)));
    return bind(self, factory, path);
  }

  /**
   * Creates a child resolver within the same scope; the child's {@link #root()} and {@link
   * #globalRoot()} are inherited unchanged from this path.
   *
   * @param key the field name under which the child is resolved; may be {@code null} for an unnamed
   *     step
   * @param factory creates the child resolver from its (extended) path
   * @return the created child resolver
   */
  public Resolver descend(@Nullable String key, Factory factory) {
    Objects.requireNonNull(factory, "factory must not be null");
    Latch self = new Latch();
    ResolverPath path = new ResolverPath(this.globalRoot, this.root, append(key, self));
    return bind(self, factory, path);
  }

  /**
   * Creates a child resolver that crosses a link/scope boundary; the created child becomes the new
   * {@link #root()}, while {@link #globalRoot()} is inherited unchanged from this path.
   *
   * @param key the field name (link) under which the child is resolved; may be {@code null} for an
   *     unnamed step
   * @param factory creates the child resolver from its (extended) path
   * @return the created child resolver
   */
  public Resolver enterScope(@Nullable String key, Factory factory) {
    Objects.requireNonNull(factory, "factory must not be null");
    Latch self = new Latch();
    ResolverPath path = new ResolverPath(this.globalRoot, self, append(key, self));
    return bind(self, factory, path);
  }

  private List<Segment> append(@Nullable String key, Supplier<Resolver> resolver) {
    List<Segment> extended = new ArrayList<>(this.segments.size() + 1);
    extended.addAll(this.segments);
    extended.add(new Segment(key, resolver));
    return extended;
  }

  private static Resolver bind(Latch self, Factory factory, ResolverPath path) {
    Resolver resolver =
        Objects.requireNonNull(factory.create(path), "factory must not return null");
    self.set(resolver);
    return resolver;
  }

  /**
   * Returns the root resolver of the outermost scope.
   *
   * @return the global root; never {@code null}
   */
  public Resolver globalRoot() {
    return this.globalRoot.get();
  }

  /**
   * Returns the root resolver of the current scope.
   *
   * @return the current-scope root; never {@code null}
   */
  public Resolver root() {
    return this.root.get();
  }

  /**
   * Returns the ordered segments from the global root to this resolver (inclusive).
   *
   * @return an unmodifiable list of segments; never empty
   */
  public List<Segment> segments() {
    return this.segments;
  }

  /**
   * Returns the last (most recently added) segment — the one for this resolver itself.
   *
   * @return the current segment
   */
  public Segment current() {
    return this.segments.get(this.segments.size() - 1);
  }

  /**
   * Returns the depth of the path, including the root segment.
   *
   * @return the number of segments
   */
  public int size() {
    return this.segments.size();
  }

  /**
   * Returns all segment keys in order; the first element is {@code null} (the root segment has no
   * key).
   *
   * @return the ordered segment keys
   */
  public List<@Nullable String> keys() {
    return Arrays.asList(this.segments.stream().map(Segment::key).toArray(String[]::new));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return (o instanceof ResolverPath that)
        && this.globalRoot.get() == that.globalRoot.get()
        && this.root.get() == that.root.get()
        && this.segments.equals(that.segments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        System.identityHashCode(this.globalRoot.get()),
        System.identityHashCode(this.root.get()),
        this.segments);
  }

  @Override
  public String toString() {
    return "ResolverPath{globalRoot=%s, root=%s, segments=%s}"
        .formatted(identity(this.globalRoot.get()), identity(this.root.get()), this.segments);
  }

  private static String identity(Resolver resolver) {
    return resolver.getClass().getSimpleName()
        + "@"
        + Integer.toHexString(System.identityHashCode(resolver));
  }

  /** A set-once holder that binds a resolver into its own path after the factory returns. */
  private static final class Latch implements Supplier<Resolver> {

    private @Nullable Resolver resolver;

    void set(Resolver resolver) {
      this.resolver = resolver;
    }

    @Override
    public Resolver get() {
      Resolver current = this.resolver;
      if (current == null) {
        throw new IllegalStateException(
            "resolver not yet bound; a factory must not query its path during construction");
      }
      return current;
    }
  }

  /**
   * One step of a {@link ResolverPath}. The root segment's {@code key} is {@code null}.
   *
   * <p>Equality and {@code toString} are intentionally identity-based for the {@code resolver}
   * component so that a {@code Segment} never depends on how resolver implementations choose to
   * implement {@link Object#equals(Object)}, {@link Object#hashCode()} or {@link
   * Object#toString()}.
   */
  public static final class Segment {

    private final @Nullable String key;

    private final Supplier<Resolver> resolver;

    Segment(@Nullable String key, Supplier<Resolver> resolver) {
      this.key = key;
      this.resolver = Objects.requireNonNull(resolver, "resolver must not be null");
    }

    /**
     * Creates a segment for an already-existing resolver.
     *
     * @param key the field name under which {@code resolver} was resolved; {@code null} for the
     *     root segment
     * @param resolver the resolver at this step; must not be {@code null}
     */
    public Segment(@Nullable String key, Resolver resolver) {
      this(key, () -> Objects.requireNonNull(resolver, "resolver must not be null"));
    }

    /**
     * Returns the field name under which the resolver was resolved.
     *
     * @return the key, or {@code null} for the root segment
     */
    public @Nullable String key() {
      return this.key;
    }

    /**
     * Returns the resolver at this step.
     *
     * @return the resolver; never {@code null}
     */
    public Resolver resolver() {
      return this.resolver.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return (o instanceof Segment that)
          && Objects.equals(this.key, that.key)
          && this.resolver.get() == that.resolver.get();
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.key, System.identityHashCode(this.resolver.get()));
    }

    @Override
    public String toString() {
      Resolver resolved = this.resolver.get();
      return "Segment{key=%s, resolver=%s@%s}"
          .formatted(
              this.key,
              resolved.getClass().getSimpleName(),
              Integer.toHexString(System.identityHashCode(resolved)));
    }
  }
}
