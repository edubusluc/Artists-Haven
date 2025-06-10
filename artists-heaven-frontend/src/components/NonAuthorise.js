const NonAuthorise = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white p-8 rounded shadow-md text-center">
                <h1 className="text-2xl font-bold text-red-600 mb-2">Acceso no autorizado</h1>
                <p className="text-gray-600">No tienes permisos para ver esta página.</p>
            </div>
        </div>
    );
}
export default NonAuthorise