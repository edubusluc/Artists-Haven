import React from "react";
import { useTranslation } from "react-i18next";


export const ModalForm = React.memo(({ title, placeholder, value, setValue, onSubmit, onCancel, isEdit }) => {
    const { t } = useTranslation();

    return (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                <h2 className="text-xl font-bold mb-4">
                    {isEdit ? `${t('adminProductList.edit')} ${title}` : `${t('adminProductList.createNew')} ${title}`}
                </h2>
                <form onSubmit={onSubmit}>
                    <input
                        type="text"
                        placeholder={placeholder}
                        value={value}
                        onChange={(e) => setValue(e.target.value)}
                        className="p-3 border border-gray-300 rounded-lg w-full mb-4 text-sm"
                    />
                    <div className="flex justify-between">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 bg-gray-300 text-black rounded-lg"
                        >
                            {t('adminProductList.cancel')}
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 bg-blue-500 text-white rounded-lg"
                        >
                            {isEdit ? t('adminProductList.saveChanges') : `${t('adminProductList.create')} ${title}`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
});

export const ModalCollectionForm = React.memo(({ title, placeholder, value, setValue, onSubmit, onCancel, isEdit, isPromoted, setIsPromoted }) => {
    const { t } = useTranslation();
    return (
        <div className="fixed inset-0 flex justify-center items-center bg-gray-500 bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                <h2 className="text-xl font-bold mb-4">
                    {isEdit ? `${t('adminProductList.edit')} ${title}` : `${t('adminProductList.createNew')} ${title}`}
                </h2>
                <form onSubmit={onSubmit}>
                    <input
                        type="text"
                        value={value}
                        onChange={(e) => setValue(e.target.value)}
                        placeholder={placeholder}
                        className="w-full border border-gray-300 rounded p-2 mb-4"
                        required
                    />
                    {isEdit && (
                        <label className="inline-flex items-center mb-4">
                            <input
                                type="checkbox"
                                checked={isPromoted}
                                onChange={(e) => setIsPromoted(e.target.checked)}
                                className="form-checkbox"
                            />
                            <span className="ml-2">{t('adminProductList.promoteCollection')}</span>
                        </label>
                    )}
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 rounded bg-gray-200"
                        >
                            {t('adminProductList.cancel')}
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 rounded bg-yellow-400 text-black font-semibold hover:bg-yellow-500"
                        >
                            {isEdit ? t('adminProductList.saveChanges') : `${t('adminProductList.create')} ${title}`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
});