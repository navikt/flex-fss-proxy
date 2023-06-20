FROM gcr.io/distroless/java17-debian11@sha256:672df6324b5e36527b201135c37c3ed14579b2eb9485a4f4e9ab526d466f671c

COPY build/libs/app.jar /app/

WORKDIR /app
CMD ["app.jar"]


ENV JDK_JAVA_OPTIONS="-Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local|*.intern.nav.no"
