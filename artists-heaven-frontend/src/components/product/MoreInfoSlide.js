import { useTranslation } from "react-i18next";
import ReactMarkdown from "react-markdown";
import SlidingPanel from "../../utils/SlidingPanel";

const MoreInfoSlide = ({ isOpen, onClose, productDetails, titleSlide }) => {
  const { t } = useTranslation();

  // Estilos para la imagen dentro del texto
  const imageStyle = {
    display: "inline-block",
    verticalAlign: "middle",
    marginRight: "0.3em",
  };

  // Estilo específico para los h2
  const h2Style = {
    textDecoration: "underline",
    color: "#696969", // Puedes cambiarlo al color que necesites
    marginTop: "10px",
    marginBottom: "10px"
  };

  const h1Style = {
    marginTop: "10px",
    marginBottom: "10px",
    textAlign: "Left"
  };

  const pStyle = {
    fontSize: "0.75rem",
    lineHeight: "1rem",
    marginTop: "10px",
  }

  const liStyle = {
    fontSize: "0.75rem",
    lineHeight: "1rem"
  }

  const components = {
    img: ({ alt, src }) => <img src={src} alt={alt} style={imageStyle} />,
    h1: ({ children }) => <h1 style={h1Style}>{children}</h1>,
    h2: ({ children }) => <h2 style={h2Style}>{children}</h2>,
    p: ({ children }) => <p style={pStyle}>{children}</p>,
    li: ({ children }) => <li style={liStyle}>{children}</li>,
  };

return (
    <SlidingPanel
      isOpen={isOpen}
      onClose={onClose}
      title={titleSlide}
      position="right"
      maxWidth="500px"
    >
      <div className="max-w-full inter-400 text-sm">
        <ReactMarkdown components={components}>
          {productDetails || t("No composition information available")}
        </ReactMarkdown>
      </div>
    </SlidingPanel>
  );
};

export default MoreInfoSlide;
