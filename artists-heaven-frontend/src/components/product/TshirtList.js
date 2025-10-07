import ProductSchema from "./ProductSchema";

const TshirtList = () => <ProductSchema endpoint="http://localhost:8080/api/product/tshirt" title="Camisetas" />;
export default TshirtList;