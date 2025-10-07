import ProductSchema from "./ProductSchema";

const PantsList = () => <ProductSchema endpoint="http://localhost:8080/api/product/pants" title="Pantalones" />;
export default PantsList;