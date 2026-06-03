package ws

import (
	"encoding/json"
	"log"
	"net/http"
	"sync"
	"time"

	"hei-gin/core/enums"

	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

// Hub maintains the set of active clients and broadcasts online counts.
type Hub struct {
	mu      sync.RWMutex
	clients map[*Client]bool

	onlineBroadcastInterval time.Duration
}

// NewHub creates a new Hub.
func NewHub() *Hub {
	return &Hub{
		clients:                 make(map[*Client]bool),
		onlineBroadcastInterval: 60 * time.Second,
	}
}

// Register adds a client to the hub.
func (h *Hub) Register(client *Client) {
	h.mu.Lock()
	h.clients[client] = true
	count := len(h.clients)
	h.mu.Unlock()

	log.Printf("[WS] Client connected: %s/%s (online: %d)", client.UserType, client.UserID, count)
}

// Unregister removes a client from the hub.
func (h *Hub) Unregister(client *Client) {
	h.mu.Lock()
	if _, ok := h.clients[client]; ok {
		delete(h.clients, client)
		close(client.Send)
	}
	count := len(h.clients)
	h.mu.Unlock()

	log.Printf("[WS] Client disconnected: %s/%s (online: %d)", client.UserType, client.UserID, count)
}

// OnlineCount returns the number of currently connected clients.
func (h *Hub) OnlineCount() int {
	h.mu.RLock()
	defer h.mu.RUnlock()
	return len(h.clients)
}

// SendToUser sends a message to a specific business (admin) user.
func (h *Hub) SendToUser(userID string, msg Message) {
	h.mu.RLock()
	defer h.mu.RUnlock()
	for client := range h.clients {
		if client.UserType == enums.LoginTypeBusiness && client.UserID == userID {
			client.SendJSON(msg)
		}
	}
}

// SendToConsumer sends a message to a specific consumer (client) user.
func (h *Hub) SendToConsumer(userID string, msg Message) {
	h.mu.RLock()
	defer h.mu.RUnlock()
	for client := range h.clients {
		if client.UserType == enums.LoginTypeConsumer && client.UserID == userID {
			client.SendJSON(msg)
		}
	}
}

// BroadcastAll sends a message to all connected clients.
func (h *Hub) BroadcastAll(msg Message) {
	data, _ := json.Marshal(msg)
	h.mu.RLock()
	defer h.mu.RUnlock()
	for client := range h.clients {
		select {
		case client.Send <- data:
		default:
		}
	}
}

// BroadcastBusiness sends a message to all connected business (admin) clients.
func (h *Hub) BroadcastBusiness(msg Message) {
	data, _ := json.Marshal(msg)
	h.mu.RLock()
	defer h.mu.RUnlock()
	for client := range h.clients {
		if client.UserType == enums.LoginTypeBusiness {
			select {
			case client.Send <- data:
			default:
			}
		}
	}
}

// StartOnlineBroadcast periodically broadcasts the online count to all clients.
func (h *Hub) StartOnlineBroadcast() {
	ticker := time.NewTicker(h.onlineBroadcastInterval)
	go func() {
		for range ticker.C {
			count := h.OnlineCount()
			h.BroadcastAll(Message{
				Type: MsgOnlineCount,
				Payload: OnlineCountPayload{
					Count: count,
				},
			})
		}
	}()
}

// HandleWebSocket upgrades an HTTP connection to WebSocket and registers the client.
func (h *Hub) HandleWebSocket(w http.ResponseWriter, r *http.Request, userID string, userType enums.LoginTypeEnum) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("[WS] Upgrade error: %v", err)
		return
	}

	client := &Client{
		Hub:      h,
		Conn:     conn,
		Send:     make(chan []byte, 256),
		UserID:   userID,
		UserType: userType,
	}

	h.Register(client)

	go client.WritePump()
	go client.ReadPump()
}

// GlobalHub is the singleton hub instance used by the application.
var GlobalHub = NewHub()
