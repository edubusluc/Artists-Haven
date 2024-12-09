import React, { useEffect, useState } from "react";

const fetchVideoBlob = async (videoUrl, authToken) => {
    try {
        const response = await fetch(`/api/admin${videoUrl}`, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch video: ${response.status}`);
        }

        const videoBlob = await response.blob();
        return URL.createObjectURL(videoBlob);
    } catch (error) {
        console.error("Error fetching video blob:", error);
        return null;
    }
};

const VerificationList = () => {
    const [verifications, setVerifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const authToken = localStorage.getItem("authToken");

    useEffect(() => {
        const fetchVerifications = async () => {
            try {
                const response = await fetch("/api/admin/verification/pending", {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        'Authorization': `Bearer ${authToken}`
                    },
                });
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                const data = await response.json();
                setVerifications(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchVerifications();
    }, [authToken]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    return (
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Artist</th>
                    <th>Video</th>
                    <th>Date</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                {verifications.map((verification) => (
                    <tr key={verification.id}>
                        <td>{verification.id}</td>
                        <td>{verification.artist?.artistName || "Unknown"}</td>
                        <td>
                            <VideoLink videoUrl={verification.videoUrl} authToken={authToken} />
                        </td>
                        <td>{new Date(verification.date).toLocaleString()}</td>
                        <td>{verification.status}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
};

const VideoLink = ({ videoUrl, authToken }) => {
    const [videoSrc, setVideoSrc] = useState(null);

    useEffect(() => {
        const fetchAndSetVideo = async () => {
            const videoBlobUrl = await fetchVideoBlob(videoUrl, authToken);
            setVideoSrc(videoBlobUrl);
        };

        fetchAndSetVideo();
    }, [videoUrl, authToken]);

    return (
        <div>
            {videoSrc ? (
                <a href={videoSrc} target="_blank" rel="noopener noreferrer">
                    Open Video
                </a>
            ) : (
                <p>Loading video...</p>
            )}
        </div>
    );
};

export default VerificationList;
