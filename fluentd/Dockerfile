FROM fluent/fluentd:edge-debian
USER root

RUN ["gem", "install", "elasticsearch", "--no-document", "--version", "8.19.0"]
RUN ["gem", "install", "fluent-plugin-elasticsearch", "--no-document", "--version", "5.4.3"]
RUN ["gem", "install", "fluent-plugin-concat", "--no-document"]
USER fluent
