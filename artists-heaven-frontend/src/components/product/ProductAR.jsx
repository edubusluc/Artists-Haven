import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, useGLTF } from '@react-three/drei';
import { Suspense, useEffect, useRef, useMemo, useState } from 'react';
import { X } from 'lucide-react'; // ícono de cierre (puedes usar Heroicons, FontAwesome, etc.)
import { Environment } from '@react-three/drei';

// ---------- Modelo 3D ----------
// ---------- Modelo 3D ----------
const TshirtModel = ({ modelReference }) => {
  const { scene } = useGLTF(`/api/product${modelReference}`);
  const modelRef = useRef();
  const [scale, setScale] = useState(() =>
    typeof window !== "undefined" && window.innerWidth < 768 ? 5 : 7
  );

  useEffect(() => {
    const handleResize = () => {
      setScale(window.innerWidth < 768 ? 5 : 7);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useMemo(() => {
    scene.traverse((child) => {
      if (child.isMesh) {
        child.castShadow = true;
        child.receiveShadow = true;
      }
    });
  }, [scene]);

  return (
    <primitive
      ref={modelRef}
      object={scene}
      scale={scale}
      position={[-1.5, -11, 0]}
      rotation={[0, Math.PI / 50, 0]}
    />
  );
};

// ---------- Modal ----------
const ProductAR = ({ onClose, modelReference, section }) => {
  const rotationCenter = section === "HOODIES" ? [0, 0, 10] : [0, 0, 0];

  useEffect(() => {
    const canvas = document.querySelector("canvas");
    const handleContextLost = (e) => {
      e.preventDefault();
      console.warn("⚠️ Contexto WebGL perdido");
    };
    canvas?.addEventListener("webglcontextlost", handleContextLost);
    return () =>
      canvas?.removeEventListener("webglcontextlost", handleContextLost);
  }, []);

  if (!modelReference) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-black/75 text-white">
        Cargando modelo...
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-md">
      <div className="relative w-[95%] md:w-[80%] lg:w-[60%] h-[80%] overflow-hidden flex flex-col ">
        <button
          onClick={onClose}
          className="absolute top-3 right-3 z-50 p-2 bg-black/50 hover:bg-black/70 rounded-full text-white transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex-1">
          <Canvas shadows camera={{ position: [0, 0, 50], fov: 45 }}>
            <ambientLight intensity={0.6} />
            <directionalLight
              position={[10, 10, 10]}
              intensity={1}
              castShadow
              shadow-mapSize-width={2048}
              shadow-mapSize-height={2048}
            />

            <Suspense fallback={null}>
              <TshirtModel modelReference={modelReference} />
              <Environment preset="warehouse" />
            </Suspense>

            <OrbitControls enableZoom enablePan={false} enableRotate target={rotationCenter} />
          </Canvas>
        </div>
      </div>
    </div>
  );
};

export default ProductAR;

