## Running

```
sbt run
```

## Testing

```
sbt test
```

## Information
Endpoint: 	GET CountEvent
				parameters: EventType: String, StartTime: epoch, EndTime: epoch
				Example request: http://localhost:8080/CountEvent?EventType=event2&StartTime=1436131568000&EndTime=1456131568000
			POST SendEvent
				parameters: json

Date Format: Epoch
Date String Format: "YYYY-MM-DD HH:mm"

Used Data Structure for event data: Vector


