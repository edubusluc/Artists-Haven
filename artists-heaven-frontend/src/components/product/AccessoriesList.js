import ProductSchema from "./ProductSchema";

const AccessoriesList = () => <ProductSchema endpoint="http://localhost:8080/api/product/accessories" title="Accesorios" />;
export default AccessoriesList;