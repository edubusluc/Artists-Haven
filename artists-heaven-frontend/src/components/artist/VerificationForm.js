import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import NonAuthorise from '../NonAuthorise';
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import { useTranslation } from 'react-i18next';

function VerificationForm() {
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { t, i18n } = useTranslation();
    const [email] = useState(localStorage.getItem("userEmail"));
    const description = `${t('verificationForm.descriptionPart1')} ${email} ${t('verificationForm.descriptionPart2')}`;
    const [video, setVideo] = useState(null);
    const role = localStorage.getItem("role");
    
    const language = i18n.language;

    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!video) {
            alert(t('attachVideoAlert'));
            return;
        }

        setLoading(true); // Inicia el feedback

        const formData = new FormData();
        formData.append('email', email);
        formData.append('description', description);
        formData.append('video', video);

        try {
            const response = await fetch(`/api/verification/send?lang=${language}`, {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': `Bearer ${authToken}`
                },
            });

            const result = await response.json();
            const message = result.message;

            if (!response.ok) {
                throw new Error(message);
            }

            alert(message);
            window.location.href = '/';
        } catch (error) {
            alert(error);
        } finally {
            setLoading(false); // Termina el feedback
        }
    };
    const handleVideoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (!file.type.startsWith('video/')) {
                alert('Por favor, seleccione un archivo de vídeo válido.');
                e.target.value = null;
                setVideo(null);
                return;
            }
            setVideo(file);
        }
    };

    if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    if (!role || role !== 'ARTIST') {
        return <NonAuthorise />;
    }

    return (
        <div
            className="min-h-screen flex items-center justify-center p-6 bg-gradient-to-r from-gray-300 to-white "
            style={{ backgroundImage: `url('/someBackground.png')`, backgroundSize: 'cover', backgroundPosition: 'center' }}
        >
            <div className="w-full max-w-3xl bg-white rounded-lg shadow-lg p-8 backdrop-blur-md">
                <h2 className="text-2xl font-bold mb-6 text-center text-gray-900">
                    {t('verificationForm.title')}
                </h2>
                <p className="mb-4 text-sm font-semibold text-red-600">
                    {t('verificationForm.maxSize')}
                </p>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label htmlFor="description" className="block mb-2 font-medium text-gray-700">
                            {t('verificationForm.description')}
                        </label>
                        <textarea
                            id="description"
                            className="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 resize-none"
                            rows="4"
                            value={description}
                            readOnly
                        />
                    </div>
                    <div>
                        <label htmlFor="video" className="block mb-2 font-medium text-gray-700">
                            {t('verificationForm.videoFile')}
                        </label>
                        <input
                            id="video"
                            type="file"
                            accept="video/*"
                            onChange={handleVideoChange}
                            className="w-full"
                        />
                    </div>
                    <div className="flex flex-col md:flex-row md:justify-between gap-4">
                        <button
                            type="submit"
                            disabled={loading}
                            className={`w-full md:w-auto py-2 px-6 rounded-lg font-semibold transition 
        ${loading ? 'bg-yellow-200 text-gray-700 cursor-not-allowed' : 'bg-yellow-400 text-black hover:bg-yellow-500'}`}
                        >
                            {loading ? (
                                <div className="flex items-center justify-center gap-2">
                                    <div className="inline-block animate-spin border-t-2 border-b-2 border-black rounded-full w-5 h-5"></div>
                                    {t('verificationForm.sending')}
                                </div>
                            ) : (
                                t('verificationForm.submit')
                            )}
                        </button>
                        <Link to="/">
                            <button
                                type="button"
                                className="w-full md:w-auto bg-gray-300 text-gray-700 py-2 px-6 rounded-lg font-semibold hover:bg-gray-400 transition"
                            >
                                {t('verificationForm.goBack')}
                            </button>
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default VerificationForm;
