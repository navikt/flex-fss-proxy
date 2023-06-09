FROM gcr.io/distroless/java17@sha256:c737fc29fc2556d3377d6a719a9842a500777fce35a7f1299acd569c73f65247

COPY build/libs/app.jar /app/

WORKDIR /app
CMD ["app.jar"]


ENV JDK_JAVA_OPTIONS="-Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local|*.intern.nav.no"
