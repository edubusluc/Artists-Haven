import { useParams } from 'react-router-dom';
import ProductSchema from './ProductSchema';

const CollectionList = () => {
  const { collectionName } = useParams();

  return (
    <ProductSchema
      endpoint={`http://localhost:8080/api/product/collection/${collectionName}`}
      title={collectionName}
      hideCollectionFilter={true}
    />
  );
};

export default CollectionList;