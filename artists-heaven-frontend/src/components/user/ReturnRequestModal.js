import { useState } from "react";

const ReturnRequestModal = ({ isOpen, onClose, onSubmit }) => {
  const [reason, setReason] = useState("");
  const [email, setEmail] = useState("");
  const authToken = localStorage.getItem("authToken");
  console.log(authToken);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();

    if (reason.trim() === "") {
      alert("Por favor, ingresa una razón para la devolución.");
      return;
    }

    if (authToken == null) {
      if (email.trim() === "") {
        alert("Por favor, ingresa el correo con el que realizaste tu orden.");
        return;
      }
      // Validación básica de formato de correo
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        alert("Por favor, ingresa un correo electrónico válido.");
        return;
      }
    }

    onSubmit(reason, email);

    setReason("");
    setEmail("");
  };

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-xl font-semibold mb-4">Reason for Return</h2>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {authToken == null && (
            <input
              type="email"
              className="border rounded-md p-2"
              placeholder="Ingresa el correo con el que realizaste tu orden"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          )}
          <textarea
            className="border rounded-md p-2 resize-none"
            rows={4}
            placeholder="Write your reason here..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => {
                setReason("");
                if (setEmail) setEmail(""); // Optional: clear email too
                onClose();
              }}
              className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 rounded bg-yellow-400 hover:bg-yellow-500 text-black font-semibold"
            >
              Submit
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReturnRequestModal;
