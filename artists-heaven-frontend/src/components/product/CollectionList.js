import { useParams } from 'react-router-dom';
import ProductSchema from './ProductSchema';

const CollectionList = () => {
  const { collectionName } = useParams();

  return (
    <ProductSchema
      endpoint={`/api/product/collection/${collectionName}`}
      title={collectionName}
      hideCollectionFilter={true}
    />
  );
};

export default CollectionList;