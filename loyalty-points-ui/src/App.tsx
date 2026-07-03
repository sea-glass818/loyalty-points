import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useRef, useState, type FormEvent } from 'react'
import './App.css'

type MessageType = 'CHAT' | 'JOIN' | 'LEAVE' | 'SYSTEM'

type ChatMessage = {
  sender?: string
  content?: string
  type: MessageType
}

type ConnectionStatus = 'Disconnected' | 'Connecting' | 'Connected' | 'Connection failed'

const SOCKET_URL = 'http://localhost:8080/ws'
const MESSAGE_TOPIC = '/topic/messages'

function App() {
  const [username, setUsername] = useState('Gina')
  const [messageText, setMessageText] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [status, setStatus] = useState<ConnectionStatus>('Disconnected')
  const stompClientRef = useRef<Client | null>(null)
  const messagesEndRef = useRef<HTMLDivElement | null>(null)
  const displayNameRef = useRef('Gina')

  const isConnected = status === 'Connected'
  const displayName = username.trim() || 'Guest'

  useEffect(() => {
    displayNameRef.current = displayName
  }, [displayName])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    return () => {
      if (stompClientRef.current?.active) {
        stompClientRef.current.publish({
          destination: '/app/leave',
          body: JSON.stringify({
            sender: displayNameRef.current,
            type: 'LEAVE',
          }),
        })
        stompClientRef.current.deactivate()
      }
    }
  }, [])

  function addSystemMessage(content: string) {
    setMessages((current) => [...current, { type: 'SYSTEM', content }])
  }

  function sendLifecycleMessage(type: 'JOIN' | 'LEAVE') {
    stompClientRef.current?.publish({
      destination: type === 'JOIN' ? '/app/join' : '/app/leave',
      body: JSON.stringify({
        sender: displayName,
        type,
      }),
    })
  }

  function handleIncomingMessage(message: IMessage) {
    const parsedMessage = JSON.parse(message.body) as ChatMessage
    setMessages((current) => [...current, parsedMessage])
  }

  function connect() {
    if (stompClientRef.current?.active) {
      return
    }

    setStatus('Connecting')

    const client = new Client({
      webSocketFactory: () => new SockJS(SOCKET_URL),
      reconnectDelay: 0,
      debug: () => undefined,
      onConnect: () => {
        setStatus('Connected')
        client.subscribe(MESSAGE_TOPIC, handleIncomingMessage)
        sendLifecycleMessage('JOIN')
      },
      onStompError: () => {
        setStatus('Connection failed')
        addSystemMessage('The server returned a STOMP error.')
      },
      onWebSocketError: () => {
        setStatus('Connection failed')
        addSystemMessage('Could not connect to http://localhost:8080/ws. Make sure Spring Boot is running.')
      },
      onDisconnect: () => {
        setStatus('Disconnected')
      },
    })

    stompClientRef.current = client
    client.activate()
  }

  async function disconnect() {
    if (!stompClientRef.current) {
      setStatus('Disconnected')
      return
    }

    sendLifecycleMessage('LEAVE')
    await stompClientRef.current.deactivate()
    stompClientRef.current = null
    setStatus('Disconnected')
  }

  function sendMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const content = messageText.trim()
    if (!content || !stompClientRef.current?.active) {
      return
    }

    stompClientRef.current.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        sender: displayName,
        content,
        type: 'CHAT',
      }),
    })

    setMessageText('')
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">LP</div>
          <div>
            <h1>Loyalty Chat</h1>
            <p>React frontend for your Spring Boot WebSocket backend.</p>
          </div>
        </div>

        <label className="field">
          <span>Display name</span>
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            disabled={isConnected}
            placeholder="Your name"
          />
        </label>

        <div className="button-grid">
          <button type="button" className="primary" onClick={connect} disabled={isConnected || status === 'Connecting'}>
            Connect
          </button>
          <button type="button" className="danger" onClick={disconnect} disabled={!isConnected}>
            Disconnect
          </button>
        </div>

        <button type="button" className="secondary" onClick={() => setMessages([])}>
          Clear chat
        </button>

        <div className="connection-card">
          <span className="eyebrow">Connection</span>
          <div className="status-row">
            <span className={`status-dot ${isConnected ? 'connected' : status === 'Connection failed' ? 'failed' : ''}`} />
            <strong>{status}</strong>
          </div>
          <code>{SOCKET_URL}</code>
        </div>
      </aside>

      <section className="chat-panel">
        <header className="chat-header">
          <div>
            <span className="eyebrow">Live topic</span>
            <h2>{MESSAGE_TOPIC}</h2>
          </div>
          <span className="message-count">{messages.length} messages</span>
        </header>

        <div className="message-list">
          {messages.length === 0 ? (
            <div className="empty-state">
              <strong>No messages yet</strong>
              <span>Connect to the backend and send a message to start testing realtime chat.</span>
            </div>
          ) : (
            messages.map((message, index) => (
              <article
                className={`message ${message.type !== 'CHAT' ? 'system' : ''} ${
                  message.sender === displayName ? 'mine' : ''
                }`}
                key={`${message.sender ?? 'system'}-${message.type}-${index}`}
              >
                {message.type === 'CHAT' ? (
                  <>
                    <div className="message-meta">
                      <strong>{message.sender || 'Guest'}</strong>
                      <span>{new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                    <p>{message.content}</p>
                  </>
                ) : (
                  <p>{message.content || `${message.sender || 'Someone'} ${message.type.toLowerCase()}ed`}</p>
                )}
              </article>
            ))
          )}
          <div ref={messagesEndRef} />
        </div>

        <form className="composer" onSubmit={sendMessage}>
          <input
            value={messageText}
            onChange={(event) => setMessageText(event.target.value)}
            disabled={!isConnected}
            placeholder={isConnected ? 'Type a message...' : 'Connect before sending messages'}
          />
          <button type="submit" className="primary" disabled={!isConnected || !messageText.trim()}>
            Send
          </button>
        </form>
      </section>
    </main>
  )
}

export default App
