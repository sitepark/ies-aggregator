package com.sitepark.ies.aggregator.value;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * A translatable wrapper around a {@link Uri} whose path segments are individually translatable.
 *
 * <p>The URI path is parsed into {@link TranslatableText} segments on creation. Rendering takes a
 * {@link Translations} table: {@link #render(Translations)} returns the fully translated URL,
 * prefixed with the table's {@link Translations#targetLang() target language}, with each path
 * segment looked up in the table. {@link #renderUriFor(Translations)} returns the same as a plain
 * {@link Uri}.
 *
 * <p>Microsite paths ({@code /microsite/<name>/...}) are detected automatically: the microsite name
 * segment is preserved verbatim while remaining path segments become translatable.
 *
 * <p>Object IDs embedded as a trailing {@code -<id>} suffix are preserved across translations.
 */
public final class TranslatableUri implements Uri, TranslatableContainer {

  private final PlainUri uri;

  private @Nullable List<TranslatableText> translatablePath;

  private @Nullable String microsite;

  private int id = -1;

  private static final Pattern ID_BASS_URL_PATTERN = Pattern.compile("-(\\d+)$");

  private TranslatableUri(PlainUri uri) {
    this.uri = uri;
  }

  /**
   * Creates a {@code TranslatableUri} from the given {@link URI}.
   *
   * @param uri the URI to wrap and parse
   */
  public static TranslatableUri of(URI uri) {
    return of(Uri.of(uri));
  }

  /**
   * Creates a {@code TranslatableUri} from the given {@link PlainUri}.
   *
   * @param uri the URI to wrap and parse
   */
  public static TranslatableUri of(PlainUri uri) {
    TranslatableUri translatableUri = new TranslatableUri(uri);
    translatableUri.initPath(uri.path());
    return translatableUri;
  }

  @SuppressWarnings(
      "StringSplitter") // blank segments are skipped, so trailing-empty behavior is moot
  private void initPath(String path) {

    if (path == null) {
      return;
    }

    Matcher matcher = ID_BASS_URL_PATTERN.matcher(path);
    if (matcher.find()) {
      try {
        this.id = Integer.parseInt(matcher.group(1));
        path = matcher.replaceAll("");
      } catch (NumberFormatException e) {
        // Ignore
      }
    }

    if (path.isBlank()) {
      return;
    }

    List<TranslatableText> translatablePath = new ArrayList<>();

    boolean readMicrositeName = false;

    for (String part : path.split("/")) {
      if (part.isBlank()) {
        continue;
      }

      if ("microsite".equals(part)) {
        readMicrositeName = true;
        continue;
      }

      if (readMicrositeName) {
        this.microsite = part;
        readMicrositeName = false;
        continue;
      }

      translatablePath.add(TranslatableText.of(part));
    }

    if (readMicrositeName) {
      microsite = "";
    }

    this.translatablePath = translatablePath;
  }

  private String renderPath(Translations translations) {

    StringBuilder path = new StringBuilder();
    if (translations.targetLang() != null) {
      path.append('/').append(translations.targetLang());
    }

    if (this.microsite != null) {
      path.append("/microsite/").append(this.microsite);
    }

    // Homepage
    if (this.translatablePath == null || this.translatablePath.isEmpty()) {
      if (this.microsite == null) {
        path.append('/');
      }
      return path.toString();
    }

    for (TranslatableText text : this.translatablePath) {
      String name = translations.translationFor(text).replace(" ", "_");
      path.append('/').append(name);
    }
    if (this.id != -1) {
      path.append('-').append(this.id);
    }
    return path.toString();
  }

  /**
   * Returns a non-translatable {@link PlainUri} with the given translations applied.
   *
   * <p>The returned URI contains the target language prefix, the translated path segments, and the
   * preserved object ID suffix.
   *
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   */
  public PlainUri renderUriFor(Translations translations) {
    return PlainUri.updatePath(this.uri, this.renderPath(translations));
  }

  /**
   * Returns the fully translated URL as a string.
   *
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   */
  public String render(Translations translations) {
    return this.renderUriFor(translations).toString();
  }

  public TranslatableUri toAbsolutePathReference() {
    TranslatableUri translatableUri = new TranslatableUri(this.uri.toAbsolutePathReference());
    return this.copyTo(translatableUri);
  }

  public TranslatableUri toAbsoluteUri(PlainUri base) {
    TranslatableUri translatableUri = new TranslatableUri(this.uri.toAbsoluteUri(base));
    return this.copyTo(translatableUri);
  }

  /**
   * Returns the non-translatable source {@link PlainUri} this instance was created from.
   *
   * <p>The translatable path segments are dropped — the returned URI is rendered verbatim and its
   * segments are no longer collected into the translation table.
   *
   * @return the underlying source {@code PlainUri}
   */
  public PlainUri plain() {
    return this.uri;
  }

  private TranslatableUri copyTo(TranslatableUri translatableUri) {
    if (this.translatablePath != null) {
      List<TranslatableText> copiedPath = new ArrayList<>(this.translatablePath.size());
      for (TranslatableText text : this.translatablePath) {
        copiedPath.add(text.copy());
      }
      translatableUri.translatablePath = copiedPath;
    }
    translatableUri.microsite = this.microsite;
    translatableUri.id = this.id;
    return translatableUri;
  }

  @Override
  public String toString() {
    return this.render(Translations.SOURCE);
  }

  @Override
  public List<TranslatableText> getTranslatableTextList() {
    if (this.translatablePath == null) {
      return List.of();
    }
    return List.copyOf(this.translatablePath);
  }
}
