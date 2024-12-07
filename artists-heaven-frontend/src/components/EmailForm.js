import React, { useState, useEffect } from 'react';

const EmailForm = () => {
    const userEmail = localStorage.getItem('userEmail');
    const [emailData, setEmailData] = useState({
        subject: '',
        sender: userEmail ? userEmail : 'Usuario No Autenticado',
        username: userEmail ? userEmail : 'Usuario No Autenticado',
        description: '',
        type: "BUG_REPORT"
    });

    const emailTypes = [
        { value: "BUG_REPORT", label: "Bug Report" },
        { value: "FEATURE_REQUEST", label: "Feature Request" },
        { value: "ABUSE_REPORT", label: "Abuse Report" },
        { value: "ISSUE_REPORT", label: "Issue Report" },
        { value: "SECURITY_REPORT", label: "Security Report" },
    ];

    useEffect(() => {
        setEmailData((prevData) => ({
            ...prevData,
            subject: prevData.type + " " + prevData.sender
        }));
    }, [emailData.type]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEmailData({ ...emailData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/api/emails/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                mode: 'cors',
                body: JSON.stringify(emailData),
            });

            if (response.ok) {
                window.location.href = '/'
            } else {
                console.error('Error enviando el email', response.statusText);
            }
        } catch (error) {
            console.error('Error enviando el email', error);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <select
                name="type"
                value={emailData.type}
                onChange={handleChange}
                required
            >
                {emailTypes.map((type) => (
                    <option key={type.value} value={type.value}>
                        {type.label}
                    </option>
                ))}
            </select>
            <textarea
                name="description"
                placeholder="Descripción"
                value={emailData.description}
                onChange={handleChange}
                required
            ></textarea>
            <button type="submit">Enviar Email</button>
        </form>
    );
};

export default EmailForm;
