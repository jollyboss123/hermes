# j-redis

A naive implementation of redis in java. Uses java 21 virtual threads.

```bash
printf "*3\r\n\$3\r\nSET\r\n\$5\r\nhello\r\n\$5\r\nworld\r\n" | nc localhost 5001
```
