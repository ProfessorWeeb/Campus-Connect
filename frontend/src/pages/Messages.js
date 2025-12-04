import axios from 'axios';
import React, { useContext, useEffect, useRef, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import './Messages.css';

const Messages = () => {
  const { user } = useContext(AuthContext);
  const [messages, setMessages] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [conversationUser, setConversationUser] = useState(null);
  const [newMessage, setNewMessage] = useState('');
  const [recipientUsername, setRecipientUsername] = useState('');
  const [isNewMessageMode, setIsNewMessageMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);

  useEffect(() => {
    if (user) {
      fetchMessages();
      // Refresh messages every 5 seconds
      const interval = setInterval(fetchMessages, 5000);
      return () => clearInterval(interval);
    }
  }, [user]);

  useEffect(() => {
    scrollToBottom();
  }, [selectedConversation, messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const fetchMessages = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/messages/inbox');
      setMessages(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching messages:', error);
      setLoading(false);
    }
  };

  const fetchConversationMessages = async (otherUserId) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/messages/direct/${otherUserId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching conversation:', error);
      return [];
    }
  };

  const handleSendDirectMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    // Determine the recipient username
    let targetRecipientUsername = recipientUsername;

    if (selectedConversation) {
      // For existing conversations, get the username from the conversation
      const conversation = conversations.find(c => c.userId === selectedConversation);
      if (conversation) {
        targetRecipientUsername = conversation.userName;
      } else {
        // Fallback: try to get username from messages
        const firstMessage = messages.find(m => 
          (m.senderId === selectedConversation && m.recipientId === user.id) ||
          (m.recipientId === selectedConversation && m.senderId === user.id)
        );
        if (firstMessage) {
          targetRecipientUsername = firstMessage.senderId === selectedConversation 
            ? firstMessage.senderName 
            : firstMessage.recipientName;
        }
      }
    }

    if (!targetRecipientUsername) {
      alert('Please select a conversation or enter a recipient username');
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/messages/direct', null, {
        params: {
          recipientUsername: targetRecipientUsername,
          content: newMessage,
        },
      });
      
      const sentMessage = response.data;
      setNewMessage('');
      // Only clear recipientUsername if we're in new message mode
      if (isNewMessageMode) {
        setRecipientUsername('');
      }
      
      // After sending, refresh messages and select the conversation
      await fetchMessages();
      
      // Find the recipient's user ID from the sent message
      const recipientId = sentMessage.recipientId === user.id ? sentMessage.senderId : sentMessage.recipientId;
      
      if (isNewMessageMode || !selectedConversation) {
        // If in new message mode, switch to the conversation
        await handleConversationSelect(recipientId);
        setIsNewMessageMode(false);
      } else if (selectedConversation) {
        // If conversation is selected, refresh conversation messages
        const conversationMessages = await fetchConversationMessages(selectedConversation);
        // Update messages state with all conversation messages
        setMessages(prevMessages => {
          // Keep messages from other conversations
          const otherMessages = prevMessages.filter(m => 
            (m.recipientId !== selectedConversation && m.senderId !== selectedConversation) ||
            (m.recipientId === selectedConversation && m.senderId === user.id) ||
            (m.senderId === selectedConversation && m.recipientId === user.id)
          );
          // Combine with fresh conversation messages
          const allMessages = [...otherMessages, ...conversationMessages];
          // Remove duplicates based on message ID
          const uniqueMessages = Array.from(
            new Map(allMessages.map(m => [m.id, m])).values()
          );
          return uniqueMessages.sort((a, b) => 
            new Date(a.createdAt) - new Date(b.createdAt)
          );
        });
      }
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMessage = error.response?.data?.message || error.response?.data?.error || 'Failed to send message';
      alert(`Failed to send message: ${errorMessage}. Please check the recipient username.`);
    }
  };

  const handleConversationSelect = async (otherUserId) => {
    setSelectedConversation(otherUserId);
    setIsNewMessageMode(false);
    setRecipientUsername('');
    const conversationMessages = await fetchConversationMessages(otherUserId);
    
    // Get the other user's info from the first message
    const firstMessage = conversationMessages[0] || messages.find(m => 
      (m.senderId === otherUserId && m.recipientId === user.id) ||
      (m.recipientId === otherUserId && m.senderId === user.id)
    );
    
    if (firstMessage) {
      setConversationUser({
        id: otherUserId,
        name: firstMessage.senderId === otherUserId ? firstMessage.senderName : firstMessage.recipientName,
      });
    }
    
    // Mark messages as read
    conversationMessages
      .filter(m => !m.isRead && m.recipientId === user.id)
      .forEach(async (msg) => {
        try {
          await axios.post(`http://localhost:8080/api/messages/${msg.id}/read`);
        } catch (e) {
          // Ignore errors
        }
      });
  };

  const handleNewMessageClick = () => {
    setIsNewMessageMode(true);
    setSelectedConversation(null);
    setConversationUser(null);
    setRecipientUsername('');
    setNewMessage('');
  };

  // Group messages by conversation (other user)
  const getConversations = () => {
    if (!user) return [];
    
    const conversationMap = new Map();
    
    messages.forEach(message => {
      const otherUserId = message.senderId === user.id ? message.recipientId : message.senderId;
      const otherUserName = message.senderId === user.id ? message.recipientName : message.senderName;
      
      if (!conversationMap.has(otherUserId)) {
        conversationMap.set(otherUserId, {
          userId: otherUserId,
          userName: otherUserName,
          messages: [],
          unreadCount: 0,
          lastMessage: null,
        });
      }
      
      const conv = conversationMap.get(otherUserId);
      conv.messages.push(message);
      
      if (!message.isRead && message.recipientId === user.id) {
        conv.unreadCount++;
      }
      
      if (!conv.lastMessage || new Date(message.createdAt) > new Date(conv.lastMessage.createdAt)) {
        conv.lastMessage = message;
      }
    });
    
    return Array.from(conversationMap.values())
      .sort((a, b) => new Date(b.lastMessage.createdAt) - new Date(a.lastMessage.createdAt));
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (days === 1) {
      return 'Yesterday';
    } else if (days < 7) {
      return date.toLocaleDateString([], { weekday: 'short' });
    } else {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    }
  };

  const conversations = getConversations();
  const conversationMessages = selectedConversation
    ? messages
        .filter(m => 
          (m.senderId === selectedConversation && m.recipientId === user?.id) ||
          (m.recipientId === selectedConversation && m.senderId === user?.id)
        )
        .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    : [];

  if (loading) {
    return <div className="container">Loading messages...</div>;
  }

  return (
    <div className="container messages-page">
      <h1>Messages</h1>
      
      <div className="messages-layout">
        <div className="conversations-sidebar">
          <div className="conversations-header">
            <h2>Conversations</h2>
            <span className="conversation-count">{conversations.length}</span>
          </div>
          
          {conversations.length === 0 ? (
            <div className="no-conversations">
              <p>No conversations yet</p>
              <p className="hint">Start a conversation by sending a message</p>
            </div>
          ) : (
            <div className="conversations-list">
              {conversations.map((conv) => (
                <div
                  key={conv.userId}
                  className={`conversation-item ${selectedConversation === conv.userId ? 'active' : ''} ${conv.unreadCount > 0 ? 'unread' : ''}`}
                  onClick={() => handleConversationSelect(conv.userId)}
                >
                  <div className="conversation-avatar">
                    {conv.userName.charAt(0).toUpperCase()}
                  </div>
                  <div className="conversation-content">
                    <div className="conversation-header">
                      <strong className="conversation-name">{conv.userName}</strong>
                      <span className="conversation-time">{formatTime(conv.lastMessage.createdAt)}</span>
                    </div>
                    <p className="conversation-preview">
                      {conv.lastMessage.content.length > 50 
                        ? conv.lastMessage.content.substring(0, 50) + '...'
                        : conv.lastMessage.content}
                    </p>
                    {conv.unreadCount > 0 && (
                      <span className="unread-badge">{conv.unreadCount}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="messages-main">
          {isNewMessageMode || (!selectedConversation && !isNewMessageMode) ? (
            <>
              <div className="conversation-header-bar">
                <div className="conversation-header-info">
                  <div className="conversation-avatar-large">
                    {recipientUsername ? recipientUsername.charAt(0).toUpperCase() : '?'}
                  </div>
                  <div className="username-input-container">
                    <input
                      type="text"
                      value={recipientUsername}
                      onChange={(e) => setRecipientUsername(e.target.value)}
                      placeholder="Enter username"
                      className="username-input"
                      autoFocus
                    />
                  </div>
                </div>
              </div>

              <div className="messages-container" ref={messagesContainerRef}>
                <div className="no-messages">
                  <p>Start a new conversation</p>
                </div>
              </div>

              <div className="message-input-container">
                <form onSubmit={handleSendDirectMessage} className="message-input-form">
                  <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Type a message..."
                    className="message-input"
                    disabled={!recipientUsername.trim()}
                  />
                  <button 
                    type="submit" 
                    className="btn-send" 
                    disabled={!newMessage.trim() || !recipientUsername.trim()}
                  >
                    Send
                  </button>
                </form>
              </div>
            </>
          ) : selectedConversation ? (
            <>
              <div className="conversation-header-bar">
                <div className="conversation-header-info">
                  <div className="conversation-avatar-large">
                    {conversationUser?.name?.charAt(0).toUpperCase() || 'U'}
                  </div>
                  <div>
                    <h3>{conversationUser?.name || 'User'}</h3>
                    <span className="conversation-status">Online</span>
                  </div>
                </div>
                <button 
                  className="btn-new-message"
                  onClick={handleNewMessageClick}
                  title="New Message"
                >
                  + New Message
                </button>
              </div>

              <div className="messages-container" ref={messagesContainerRef}>
                {conversationMessages.length === 0 ? (
                  <div className="no-messages">
                    <p>No messages yet. Start the conversation!</p>
                  </div>
                ) : (
                  conversationMessages.map((message) => {
                    const isSent = message.senderId === user?.id;
                    return (
                      <div
                        key={message.id}
                        className={`message-bubble ${isSent ? 'sent' : 'received'} ${!message.isRead && !isSent ? 'unread' : ''}`}
                      >
                        {!isSent && (
                          <div className="message-sender-avatar">
                            {message.senderName.charAt(0).toUpperCase()}
                          </div>
                        )}
                        <div className="message-content">
                          {!isSent && (
                            <div className="message-sender-name">{message.senderName}</div>
                          )}
                          <div className="message-text">{message.content}</div>
                          <div className="message-meta">
                            <span className="message-time">{formatTime(message.createdAt)}</span>
                            {isSent && (
                              <span className="message-status">
                                {message.isRead ? '✓✓' : '✓'}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              <div className="message-input-container">
                <form onSubmit={handleSendDirectMessage} className="message-input-form">
                  <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Type a message..."
                    className="message-input"
                  />
                  <button type="submit" className="btn-send" disabled={!newMessage.trim()}>
                    Send
                  </button>
                </form>
              </div>
            </>
          ) : (
            <div className="new-conversation-view">
              <div className="new-conversation-header">
                <h2>Start New Conversation</h2>
                <button 
                  className="btn-new-message-header"
                  onClick={handleNewMessageClick}
                >
                  + New Message
                </button>
              </div>
              <div className="new-conversation-content">
                <p className="new-conversation-hint">Click "New Message" to start a conversation</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Messages;

