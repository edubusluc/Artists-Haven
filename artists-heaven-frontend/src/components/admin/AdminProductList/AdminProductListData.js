// hooks/useAdminData.js
import { useCallback, useState } from "react";
import { getAllCategories, getAllProducts, getAllCollections, getProductManagement } from "../../../services/adminServices";

export const useAdminData = (authToken, setTotalPages) => {
    const [categories, setCategories] = useState([]);
    const [collections, setCollections] = useState([]);
    const [products, setProducts] = useState([]);
    const [productManagement, setProductManagement] = useState({});

    const fetchCategories = useCallback(async () => {
        const { data } = await getAllCategories(authToken);
        setCategories(data);
    }, [authToken]);

    const fetchCollections = useCallback(async () => {
        const { data } = await getAllCollections(authToken);
        setCollections(data);
    }, [authToken]);

    const fetchProducts = useCallback(async (filters = {}) => {
        try {
            const {
                page = 0,
                pageSize = 6,
                searchTerm = "",
                available = null,
                promoted = null,
            } = filters;

            const response = await getAllProducts(
                authToken,
                page,
                pageSize,
                searchTerm,
                available,
                promoted
            );
            setProducts(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            console.error("Error fetching products:", err);
        }
    }, [authToken, setTotalPages]);

    const fetchProductManagement = useCallback(async () => {
        const { data } = await getProductManagement(authToken);
        setProductManagement(data);
    }, [authToken]);

    return { categories, collections, products, productManagement, fetchCategories, fetchCollections, fetchProducts, fetchProductManagement };
};
