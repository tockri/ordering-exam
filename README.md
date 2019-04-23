# How to run

1. start mysql & sbt
    ```bash
    $ docker-compose run --rm app sbt
    ```
    (wait for several minutes patiently)
2. Make Initial Data
    ```bash
    sbt:ordering-exam> run create 
    ```
3. Test
    ```bash
    sbt:ordering-exam> run test
    ```
