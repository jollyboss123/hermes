# pegasus

pegasus is a naive Java implementation of a Redis server that can be connected to using the Jedis client.

## Installation and Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/pegasus.git
   ```

2. Build the project using maven:

    ```bash
    cd j-redis
    mvn clean package
    ```

3. Run the pegasus server

    ```bash
    java -jar target/j-redis.jar
    ```
   
## Usage
### Using Jedis Client
You can connect to the j-redis server using the Jedis client as follows:
```java
try (Jedis jedis = new Jedis("localhost", <port>)) {
    jedis.set("hello", "world");
    String value = jedis.get("hello");
    jedis.get("nonexistent");
}
```

### Sending Commands Manually
Alternatively, you can send Redis commands manually:
#### Set Command
```bash
printf "*3\r\n\$3\r\nSET\r\n\$5\r\nhello\r\n\$5\r\nworld\r\n" | nc localhost <port>
```

#### Get Command
```bash
printf "*3\r\n\$3\r\nGET\r\n\$5\r\nhello\r\n" | nc localhost <port>
```
