package com.flash.film.common.config.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RedoclyController {

    @Value("${server.port}")
    private int port;

    @Value("${springdoc.api-docs.path}")
    private String apiDocsPath;

    @GetMapping(value = "/redoc", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String redoc() {
        String specUrl = "http://localhost:" + port + apiDocsPath;
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Film BE — API Documentation</title>
                    <link rel="icon" type="image/png" href="https://redocly.com/favicon.ico"/>
                    <style>
                        body { margin: 0; padding: 0; }
                    </style>
                </head>
                <body>
                    <redoc spec-url='""" + specUrl + """
                '
                        hide-download-button
                        theme='{"colors":{"primary":{"main":"#7C3AED"}},"typography":{"fontSize":"15px","headings":{"fontFamily":"Inter,sans-serif"}}}'
                    ></redoc>
                    <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
                </body>
                </html>
                """;
    }
}
