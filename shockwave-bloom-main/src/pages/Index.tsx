import ShaderBackground from "@/components/ShaderBackground";
import HeroSection from "@/components/landing/HeroSection";
import FeaturesSection from "@/components/landing/FeaturesSection";
import CTASection from "@/components/landing/CTASection";

const Index = () => {
  return (
    <div className="relative min-h-screen overflow-x-hidden">
      <ShaderBackground />
      
      <main className="relative z-10">
        <HeroSection />
        <FeaturesSection />
        <CTASection />
        
        {/* Footer */}
        <footer className="py-8 px-4 border-t border-border/30">
          <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="gold-text font-bold text-xl">LoanSmart</div>
            <p className="text-sm text-muted-foreground">
              © 2024 LoanSmart. All rights reserved.
            </p>
          </div>
        </footer>
      </main>
    </div>
  );
};

export default Index;
