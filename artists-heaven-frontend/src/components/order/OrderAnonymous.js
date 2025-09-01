import { useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import ReturnRequestModal from '../user/ReturnRequestModal';
import { useTranslation } from 'react-i18next';

const OrderAnonymous = () => {
  const location = useLocation();
  const response = location.state?.order;

  const [order, setOrder] = useState(null);
  const [productImages, setProductImages] = useState({});
  const authToken = localStorage.getItem('authToken');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

  const orderSteps = ['PAID', 'IN_PREPARATION', 'SENT', 'DELIVERED'];
  const { t, i18n } = useTranslation();
  const currentLang = i18n.language;

  useEffect(() => {
    if (response.orders) {
      setOrder(response.orders);
      setProductImages(response.productImages);
    } else {
      console.log('Response is falsy:', response);
    }
  }, [response]);

  if (!order) {
    return <div className="p-8 text-center text-gray-500">{t('orderAnonymous.loadingOrder')}</div>;
  }

  const handleOpenModal = (orderId) => {
    setSelectedOrderId(orderId);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedOrderId(null);
  };

  const handleSubmitReason = async (reason, email) => {
    try {
      const res = await fetch(`/api/returns/create?lang=${currentLang}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${authToken}`,
        },
        body: JSON.stringify({
          orderId: selectedOrderId,
          reason: reason,
          email: email,
        }),
      });

      const response = await res.json();

      if (!res.ok) {
        throw new Error(response.message);
      }
      alert(response.message || 'Return request created successfully');

      setOrder((prev) => ({ ...prev, status: 'RETURN_REQUEST' }));
      handleCloseModal();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDownloadLabel = async (orderId) => {
    let emailParam = '';

    if (!authToken) {
      const email = prompt('Por favor, introduce el correo electrónico de la compra:');

      if (!email || !email.trim()) {
        alert('Correo electrónico requerido para continuar.');
        return;
      }

      emailParam = `?email=${encodeURIComponent(email.trim())}`;
    }

    const response = await fetch(`/api/returns/${orderId}/label${emailParam}`, {
      method: 'GET',
      headers: {
        ...(authToken && { Authorization: `Bearer ${authToken}` }),
      },
    });

    if (!response.ok) {
      alert('Error al descargar la etiqueta de devolución');
      return;
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `RETURN_LABEL_${orderId}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  console.log(order.items)

  return (
    <>
      <div className="bg-white p-8 rounded-xl shadow flex flex-col gap-4 mt-10 m-4">
        {order.status === 'DELIVERED' && (
          <div className="text-right">
            <button
              onClick={() => handleOpenModal(order.id)}
              className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition hover:bg-yellow-500"
            >
              {t('orderAnonymous.requestReturn')}
            </button>
          </div>
        )}
        {order.status === 'RETURN_REQUEST' && (
          <div className="text-right">
            <button
              onClick={() => handleDownloadLabel(order.id)}
              className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition hover:bg-yellow-500"
            >
              {t('orderAnonymous.downloadReturnLabel')}
            </button>
          </div>
        )}

        <OrderProgressBar status={order.status} orderSteps={orderSteps} />

        <div className="flex justify-between items-center">
          <div>
            <h3 className="text-xl font-semibold">{t('orderAnonymous.order')} #{order.identifier}</h3>
            <p className="inter-400 text-sm text-gray-500">{t('orderAnonymous.status')}: {order.status}</p>
          </div>
          <div className="text-right">
            <p className="inter-400 text-sm text-green-600">€{order.totalPrice.toFixed(2)}</p>
          </div>
        </div>

        <div className="text-sm text-gray-600">
          <p className='inter-400 text-sm'>{t('orderAnonymous.shippingAddress')}: {order.addressLine1}</p>
        </div>

        <div className="flex flex-col gap-4 text-sm text-gray-700">
          {order.items.map((item) => {
            const imagePath = productImages[item.productId];
            return (
              <div
                key={item.id}
                className="border rounded-lg p-4 bg-gray-50 shadow-sm flex flex-col md:flex-row"
              >
                <div className="w-full md:w-32 flex-shrink-0">
                  <img
                    src={imagePath ? `/api/product${imagePath}` : '/placeholder.jpg'}
                    alt={item.name}
                    className="w-full h-32 object-contain rounded-md"
                    onError={(e) => (e.target.src = '/placeholder.jpg')}
                    loading="lazy"
                  />
                </div>
                <div className="flex flex-col justify-center w-full">
                  <p className="inter-400 text-sm">{t('orderAnonymous.product')}: {item.name}</p>
                  {item.section !== "ACCESSORIES" &&
                    <p className='inter-400 text-sm'>{t('orderAnonymous.size')}: {item.size}</p>}
                  <p className='inter-400 text-sm'>{t('orderAnonymous.quantity')}: {item.quantity}</p>
                  <p className="text-green-700 inter-400 text-sm">{item.price.toFixed(2)}€</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
      <ReturnRequestModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleSubmitReason}
      />
    </>
  );
};

export default OrderAnonymous;

/**
 * Barra de progreso sensible a móvil: vertical en pantallas pequeñas y horizontal en ≥sm.
 * - En móvil (default): stepper vertical con iconos, más legible y táctil.
 * - En escritorio (sm+): barra horizontal con conectores.
 */
const OrderProgressBar = ({ status, orderSteps }) => {
  // Estados especiales
  const { t } = useTranslation();
  if (status === 'CANCELED') {
    return (
      <div className="p-4 border border-red-300 bg-red-50 rounded-lg text-red-600 text-center font-semibold">
        {t('myOrders.orderCanceled')}
      </div>
    );
  }

  if (status === 'RETURN_REQUEST') {
    return (
      <div className="p-4 border border-yellow-300 bg-yellow-50 rounded-lg text-yellow-700 text-center font-semibold">
        {t('myOrders.alreadyReturnRequest')}
      </div>
    );
  }

  const displayStatus =
    status === 'RETURN_REQUEST' || status === 'RETURN_ACCEPTED' ? 'DELIVERED' : status;

  const currentStepIndex = orderSteps.indexOf(displayStatus);

  return (
    <div className="w-full mt-10" aria-label="Order progress" role="group">
      {/* MOBILE: Vertical stepper */}
      <ol className="sm:hidden relative border-s-2 border-gray-200 pl-4">
        {orderSteps.map((step, index) => {
          const isCompleted = index < currentStepIndex;
          const isCurrent = index === currentStepIndex;

          return (
            <li key={step} className="mb-5 last:mb-0">
              <div className="absolute -start-[10px] mt-1">
                <span
                  className={[
                    'flex h-5 w-5 items-center justify-center rounded-full text-[10px] font-bold shadow-sm ring-2',
                    isCompleted
                      ? 'bg-green-500 text-white ring-green-200'
                      : isCurrent
                        ? 'bg-white text-green-600 ring-green-400'
                        : 'bg-gray-200 text-gray-600 ring-gray-200',
                  ].join(' ')}
                  aria-current={isCurrent ? 'step' : undefined}
                >
                  {isCompleted ? '✓' : index + 1}
                </span>
              </div>

              <div className="ms-6">
                <p
                  className={[
                    'text-sm font-semibold',
                    isCompleted || isCurrent ? 'text-gray-900' : 'text-gray-500',
                  ].join(' ')}
                >
                  {step.replace('_', ' ')}
                </p>
                <p className="text-xs text-gray-500">
                  {isCompleted && 'Completado'}
                  {isCurrent && !isCompleted && 'En curso'}
                  {!isCompleted && !isCurrent && 'Pendiente'}
                </p>
              </div>
            </li>
          );
        })}
      </ol>

      {/* DESKTOP/TABLET: Horizontal progress bar */}
      <div className="hidden sm:block">
        <div className="flex items-center justify-between relative mb-3">
          {orderSteps.map((step, index) => {
            const isCompleted = index <= currentStepIndex;
            const isLast = index === orderSteps.length - 1;

            return (
              <div key={step} className="flex-1 flex items-center min-w-[70px] relative">
                {index !== 0 && (
                  <div className={`h-1 flex-1 ${index <= currentStepIndex ? 'bg-green-500' : 'bg-gray-200'}`} />
                )}

                <div
                  className={[
                    'w-7 h-7 z-10 flex items-center justify-center rounded-full text-xs font-bold shadow-sm ring-2',
                    isCompleted
                      ? 'bg-green-500 text-white ring-green-200'
                      : 'bg-white text-gray-700 ring-gray-300',
                  ].join(' ')}
                >
                  {index + 1}
                </div>

                {!isLast && (
                  <div className={`h-1 flex-1 ${index < currentStepIndex ? 'bg-green-500' : 'bg-gray-200'}`} />
                )}
              </div>
            );
          })}
        </div>

        <div className="flex text-xs text-gray-600 font-medium px-1">
          {orderSteps.map((step, index) => (
            <div
              key={index}
              className={[
                'flex-1 min-w-[70px]',
                step === 'PAID' && 'text-left',
                step === 'IN_PREPARATION' && 'text-center',
                step === 'SENT' && 'text-center',
                step === 'DELIVERED' && 'text-right',
              ]
                .filter(Boolean)
                .join(' ')}
            >
              {t(step.replace("_", " "))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
