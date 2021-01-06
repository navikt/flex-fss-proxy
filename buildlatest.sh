echo "Bygger flex-fss-proxy latest"

./gradlew bootJar

docker build . -t flex-fss-proxy:latest
