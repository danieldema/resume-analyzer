import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Landing } from './pages/Landing';
import { Target } from './pages/Target';
import { Skills } from './pages/Skills';
import { Results } from './pages/Results';
import { Privacy } from './pages/Privacy';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/target" element={<Target />} />
        <Route path="/skills" element={<Skills />} />
        <Route path="/results" element={<Results />} />
        <Route path="/privacy" element={<Privacy />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
