import React, { useState, useEffect, useRef } from "react";

const Chatbot = () => {
  const [messages, setMessages] = useState([
    { text: "Hola! Soy Quavi tu asistente de confianza, ¿en qué puedo ayudarte?", from: "bot" },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const messagesEndRef = useRef(null);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    const newMessages = [...messages, { text: input, from: "user" }];
    setMessages(newMessages);
    setInput("");
    setLoading(true);

    try {
      const response = await fetch("/api/chatbot/message", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: input }),
      });

      if (!response.ok) throw new Error("Error en la respuesta del servidor");

      const data = await response.json();
      setMessages([...newMessages, { text: data.reply, from: "bot" }]);
    } catch (error) {
      setMessages([...newMessages, { text: "Error al contactar con el bot.", from: "bot" }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") sendMessage();
  };

  useEffect(() => {
    if(open) messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, open]);

  return (
    <>
      {/* Botón flotante circular */}
      {!open && (
        <button
          onClick={() => setOpen(true)}
          aria-label="Abrir chat"
          style={{
            position: "fixed",
            bottom: 20,
            right: 20,
            width: 60,
            height: 60,
            borderRadius: "50%",
            backgroundColor: "#4a90e2",
            border: "none",
            color: "white",
            fontSize: 30,
            cursor: "pointer",
            boxShadow: "0 4px 12px rgba(74,144,226,0.7)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
            transition: "background-color 0.3s",
          }}
          onMouseEnter={e => (e.currentTarget.style.backgroundColor = "#357ABD")}
          onMouseLeave={e => (e.currentTarget.style.backgroundColor = "#4a90e2")}
        >
          💬
        </button>
      )}

      {/* Ventana de chat desplegada */}
      {open && (
        <div
          style={{
            position: "fixed",
            bottom: 20,
            right: 20,
            width: 350,
            maxWidth: "90vw",
            height: 450,
            borderRadius: 15,
            boxShadow: "0 8px 24px rgba(0,0,0,0.25)",
            padding: 15,
            fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
            backgroundColor: "#fff",
            display: "flex",
            flexDirection: "column",
            zIndex: 1000,
            animation: "fadeIn 0.3s ease",
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 10,
            }}
          >
            <strong>Quavi</strong>
            <button
              onClick={() => setOpen(false)}
              aria-label="Cerrar chat"
              style={{
                background: "transparent",
                border: "none",
                fontSize: 22,
                fontWeight: "bold",
                cursor: "pointer",
                color: "#999",
                padding: 0,
                lineHeight: 1,
              }}
              onMouseEnter={e => (e.currentTarget.style.color = "#333")}
              onMouseLeave={e => (e.currentTarget.style.color = "#999")}
            >
              ×
            </button>
          </div>

          <div
            style={{
              flex: 1,
              overflowY: "auto",
              padding: "10px 15px",
              backgroundColor: "#f7f9fc",
              borderRadius: 12,
              boxShadow: "inset 0 2px 5px rgba(0,0,0,0.05)",
              marginBottom: 15,
              display: "flex",
              flexDirection: "column",
              gap: 12,
            }}
          >
            {messages.map((msg, index) => (
              <div
                key={index}
                style={{
                  display: "flex",
                  justifyContent: msg.from === "bot" ? "flex-start" : "flex-end",
                }}
              >
                <div
                  style={{
                    backgroundColor: msg.from === "bot" ? "#e3eafc" : "#4a90e2",
                    color: msg.from === "bot" ? "#333" : "#fff",
                    padding: "10px 16px",
                    borderRadius: 20,
                    maxWidth: "75%",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                    fontSize: 14,
                    lineHeight: 1.4,
                    whiteSpace: "pre-wrap",
                    wordBreak: "break-word",
                  }}
                >
                  {msg.text}
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
          <div style={{ display: "flex", gap: 10 }}>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Escribe tu mensaje..."
              disabled={loading}
              style={{
                flex: 1,
                padding: "12px 16px",
                borderRadius: 25,
                border: "1.5px solid #ddd",
                fontSize: 15,
                outline: "none",
                transition: "border-color 0.3s",
              }}
              onFocus={(e) => (e.target.style.borderColor = "#4a90e2")}
              onBlur={(e) => (e.target.style.borderColor = "#ddd")}
            />
            <button
              onClick={sendMessage}
              disabled={loading || !input.trim()}
              style={{
                backgroundColor: loading || !input.trim() ? "#a1c3f7" : "#4a90e2",
                border: "none",
                borderRadius: 25,
                padding: "0 20px",
                color: "white",
                fontWeight: "600",
                fontSize: 15,
                cursor: loading || !input.trim() ? "not-allowed" : "pointer",
                boxShadow: loading || !input.trim() ? "none" : "0 4px 12px rgba(74,144,226,0.5)",
                transition: "background-color 0.3s, box-shadow 0.3s",
              }}
            >
              {loading ? "Enviando..." : "Enviar"}
            </button>
          </div>
        </div>
      )}

      {/* Animación fadeIn para el chat */}
      <style>
        {`
          @keyframes fadeIn {
            from {opacity: 0; transform: translateY(10px);}
            to {opacity: 1; transform: translateY(0);}
          }
        `}
      </style>
    </>
  );
};

export default Chatbot;
