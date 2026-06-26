class EventProducer:
    async def publish(self, topic: str, payload: dict) -> None:
        _ = (topic, payload)
