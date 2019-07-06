/*
 * Copyright 2019-2019 Gryphon Zone
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zone.gryphon.tumblr.downloader;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tyrol
 */
@Slf4j
@ToString
public class TumblrDownloaderApplication {

    public static void main(String... args) throws Exception {
        new TumblrDownloaderApplication().doMain(args);
    }

    @Option(name = "--tumblr", required = true)
    private String tumblr;

    @Option(name = "--consumer-key", required = true)
    private String consumerKey;

    @Option(name = "--consumer-secret", required = true)
    private String consumerSecret;

    public void doMain(String... args) throws Exception {
        new CmdLineParser(this).parseArgument(args);

        JumblrClient client = new JumblrClient(this.consumerKey, this.consumerSecret);

        Blog blog = client.blogInfo(tumblr);
        int offset = 0;

        while (true) {
            Map<String, Integer> options = new HashMap<>();
            options.put("offset", offset);
            List<Post> posts = blog.posts(options);

            if (posts == null || posts.size() == 0) {
                break;
            }

            for (Post post : posts) {
                if (post instanceof PhotoPost) {
                    PhotoPost photoPost = (PhotoPost) post;

                    for (Photo photo : photoPost.getPhotos()) {

                        String url = photo.getOriginalSize().getUrl();

                        log.info("Original photo size: {}", url);

                        try (InputStream in = new URL(url).openStream()) {
                            Files.copy(in, Paths.get("download/" + url.substring(url.lastIndexOf("/") + 1)));
                        }
                    }

                } else {
                    log.info("Ignoring post of type {} with url {}", post.getClass().getSimpleName(), post.getPostUrl());
                }

                offset++;
            }
        }
    }
}
