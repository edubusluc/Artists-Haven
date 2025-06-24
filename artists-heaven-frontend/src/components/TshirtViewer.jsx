import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, useGLTF } from '@react-three/drei';
import { Suspense, useEffect, useRef } from 'react';
import bgMainShop from '../util-image/bgMainShop.png';

const TshirtModel = () => {
  const { scene } = useGLTF('/models/tshirt.glb');
  const modelRef = useRef();

  useFrame(() => {
    if (modelRef.current) {
      modelRef.current.rotation.y += 0.002;
    }
  });

  scene.traverse((child) => {
    if (child.isMesh) {
      child.castShadow = true;
      child.receiveShadow = true;
    }
  });

  return <primitive ref={modelRef} object={scene} scale={2} position={[0, -45, 0]} />;
};

const TshirtViewer = () => {
  useEffect(() => {
    const canvas = document.querySelector('canvas');
    const handleContextLost = (e) => {
      e.preventDefault();
      console.warn('⚠️ Contexto WebGL perdido');
    };
    canvas?.addEventListener('webglcontextlost', handleContextLost);
    return () => canvas?.removeEventListener('webglcontextlost', handleContextLost);
  }, []);

  return (
    <div
      className="relative w-full h-[600px] md:h-[800px] overflow-hidden"
      style={{
        backgroundImage: `url(${bgMainShop})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
      }}
    >
      {/* Modelo 3D */}
      <Canvas
        shadows
        camera={{ position: [0, 0, 200], fov: 45 }}
        gl={{ preserveDrawingBuffer: true }}
      >
        <ambientLight intensity={0.5} />
        <directionalLight position={[5, 5, 5]} intensity={2} castShadow />
        <Suspense fallback={null}>
          <TshirtModel />
        </Suspense>
        <OrbitControls enablePan enableZoom={false} enableRotate />
      </Canvas>

      {/* Texto por delante */}
      <div className="absolute top-[60%] md:top-[65%] left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-center z-20 pointer-events-none px-4">
        <h2 className="main-view-font font-light uppercase italic tracking-widest
                 text-xl sm:text-2xl md:text-3xl lg:text-4xl leading-tight drop-shadow">
          ARTISTS
        </h2>
        <h1 className="main-view-font font-extralight uppercase italic tracking-wider
                 text-4xl sm:text-6xl md:text-8xl lg:text-[100px] leading-none drop-shadow-2xl mb-5">
          HEAVEN
        </h1>
        <p className="main-view-subtitle text-sm sm:text-base md:text-lg tracking-wide leading-snug drop-shadow">
          WHERE EVERY BEAT AND BRUSHSTROKE FINDS ITS HOME
        </p>
      </div>

      {/* Botón de orden */}
      <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 z-10">
        <button className="button-yellow-border">
          ORDER NOW
        </button>
      </div>
    </div>
  );
};

export default TshirtViewer;
