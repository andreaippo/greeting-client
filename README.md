Run with:

`podman build -f Dockerfile -t org.example.greeting-client .`

Create bridge network if it does not exist:

`podman network create --driver bridge andrea_bridge_default`

`podman run --replace -e SPRING_PROFILES_ACTIVE=container --net andrea_bridge_default -p 8070:8070 --name=greeting-client -d localhost/org.example.greeting-client:latest`

Test with

`watch -n 1 curl localhost:8070/greeting-client` 