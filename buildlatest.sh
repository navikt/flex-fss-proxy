echo "Bygger flex-fss-proxy latest"

mvn clean install

docker build . -t flex-fss-proxy:latest
