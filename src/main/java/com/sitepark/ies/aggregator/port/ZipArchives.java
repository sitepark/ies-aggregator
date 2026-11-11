package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.media.Media;
import com.sitepark.ies.aggregator.value.media.ZipArchive;
import java.util.List;
import java.util.Optional;

/**
 * Packs media into a zip archive that is published with the resource being aggregated.
 *
 * <p>This is the port for the legacy SPML tag {@code sp:zip}. The archive is a file of the
 * resource, not a part of its output: it lies in the {@code zips} folder of the media directory of
 * the object being published, next to the page, and is generated again with it. What the
 * aggregator gets back is only the description it needs to link the archive - see {@link
 * ZipArchive}.
 *
 * <p>The archive holds the original file of every medium, stored without compression, under the
 * medium's file name. Two media with the same file name cannot share an archive; the legacy tag
 * fails on them, and so does {@link #create}: it answers empty.
 */
public interface ZipArchives {

  /**
   * Creates the archive of the given media and publishes it with the resource.
   *
   * <p>The file name is built from {@code name} by {@link ZipArchive#filenameOf(String)}. An
   * archive of the same name that an earlier generation left is replaced. A medium that is not
   * known to the store is left out; a list without any medium still yields an (empty) archive, as
   * the legacy tag does.
   *
   * @param name the name the archive is given, usually the name of the gallery it holds; characters
   *     a file name must not carry are replaced
   * @param media the media to pack, in the order they enter the archive
   * @return the archive, or empty if it could not be created
   */
  Optional<ZipArchive> create(String name, List<? extends Media> media);
}
