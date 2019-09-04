package pl.cyfronet.s4e.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import pl.cyfronet.s4e.BasicTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@BasicTest
public class MarkdownHtmlUtilTest {

    @Test
    public void shouldReturnParagraph() {
        String result = MarkdownHtmlUtil.markdownToStringHtml("This is paragraph");
        assertThat(result, is(equalTo("<p>This is paragraph</p>\n")));
    }

    @Test
    public void shouldReturnHeader() {
        String result = MarkdownHtmlUtil.markdownToStringHtml("This is header lvl 1\n" +
                "===========================\n" +
                "This is header lvl 2\n" +
                "-------------------------");
        assertThat(result, is(equalTo("<h1>This is header lvl 1</h1>\n<h2>This is header lvl 2</h2>\n")));
    }

    @Test
    public void shouldReturnList() {
        String result = MarkdownHtmlUtil.markdownToStringHtml("*   Things.\n" +
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
        String result = MarkdownHtmlUtil.markdownToStringHtml("1.   Things.\n" +
                "2.   More Things.\n" +
                "3.   Even More Things.");
        assertThat(result, is(equalTo("<ol>\n" +
                "<li>Things.</li>\n" +
                "<li>More Things.</li>\n" +
                "<li>Even More Things.</li>\n" +
                "</ol>\n")));
    }
}
