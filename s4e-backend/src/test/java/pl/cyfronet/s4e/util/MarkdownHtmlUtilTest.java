/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@BasicTest
public class MarkdownHtmlUtilTest {

    @Autowired
    MarkdownHtmlUtil markdownHtmlUtil;

    @Test
    public void shouldReturnParagraph() {
        String result = markdownHtmlUtil.markdownToStringHtml("This is paragraph");
        assertThat(result, is(equalTo("<p>This is paragraph</p>\n")));
    }

    @Test
    public void shouldReturnHeader() {
        String result = markdownHtmlUtil.markdownToStringHtml("This is header lvl 1\n" +
                "===========================\n" +
                "This is header lvl 2\n" +
                "-------------------------");
        assertThat(result, is(equalTo("<h1>This is header lvl 1</h1>\n<h2>This is header lvl 2</h2>\n")));
    }

    @Test
    public void shouldReturnList() {
        String result = markdownHtmlUtil.markdownToStringHtml("*   Things.\n" +
                "*   More Things.\n" +
                "*   Even More Things.");
        assertThat(result, is(equalTo("<ul>\n" +
                "<li>Things.</li>\n" +
                "<li>More Things.</li>\n" +
                "<li>Even More Things.</li>\n" +
                "</ul>\n")));
    }

    @Test
    public void shouldReturnNumberedList() {
        String result = markdownHtmlUtil.markdownToStringHtml("1.   Things.\n" +
                "2.   More Things.\n" +
                "3.   Even More Things.");
        assertThat(result, is(equalTo("<ol>\n" +
                "<li>Things.</li>\n" +
                "<li>More Things.</li>\n" +
                "<li>Even More Things.</li>\n" +
                "</ol>\n")));
    }
}
