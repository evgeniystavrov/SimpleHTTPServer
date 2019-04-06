package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.config.HttpServerConfig;
// class class required to access to server config
class AbstractHttpConfigurableComponent {
    final HttpServerConfig httpServerConfig;

    AbstractHttpConfigurableComponent(HttpServerConfig httpServerConfig) {
        super();
        this.httpServerConfig = httpServerConfig;
    }
}
