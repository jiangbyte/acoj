package eventbus

import (
	"log"
	"sync"

	"hei-gin/api"
)

// DefaultBus is the global event bus instance.
var DefaultBus api.EventBus = newBus()

type bus struct {
	mu          sync.RWMutex
	subscribers map[string][]api.EventSubscriber
	closed      chan struct{}
	wg          sync.WaitGroup
}

func newBus() *bus {
	return &bus{
		subscribers: make(map[string][]api.EventSubscriber),
		closed:      make(chan struct{}),
	}
}

func (b *bus) Publish(topic string, data any) {
	b.mu.RLock()
	subs, ok := b.subscribers[topic]
	b.mu.RUnlock()
	if !ok {
		return
	}
	event := api.Event{Topic: topic, Data: data}
	for _, sub := range subs {
		sub := sub
		b.wg.Add(1)
		go func() {
			defer b.wg.Done()
			defer func() {
				if r := recover(); r != nil {
					log.Printf("[EventBus] subscriber panic on topic %s: %v", topic, r)
				}
			}()
			select {
			case <-b.closed:
				return
			default:
				sub(event)
			}
		}()
	}
}

func (b *bus) Subscribe(topic string, sub api.EventSubscriber) func() {
	b.mu.Lock()
	b.subscribers[topic] = append(b.subscribers[topic], sub)
	b.mu.Unlock()

	return func() {
		b.mu.Lock()
		defer b.mu.Unlock()
		subs := b.subscribers[topic]
		for i, s := range subs {
			if &s == &sub {
				b.subscribers[topic] = append(subs[:i], subs[i+1:]...)
				return
			}
		}
	}
}

// Topics used across the system.
const (
	TopicUserConnected    = "user:connected"
	TopicUserDisconnected = "user:disconnected"
	TopicMessageSent      = "message:sent"
	TopicMessageRead      = "message:read"
)
