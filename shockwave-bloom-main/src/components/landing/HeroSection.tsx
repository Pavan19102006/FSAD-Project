import { Button } from "@/components/ui/button";
import { ArrowRight, ChevronDown } from "lucide-react";

export default function HeroSection() {
  const scrollToFeatures = () => {
    document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <section className="relative min-h-screen flex flex-col items-center justify-center px-4 overflow-hidden">
      <div className="max-w-5xl mx-auto text-center z-10">
        {/* Badge */}
        <div className="inline-flex items-center gap-2 px-4 py-2 mb-8 rounded-full glass-card border border-primary/30 animate-fade-up" style={{ animationDelay: '0.1s' }}>
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
          <span className="text-sm text-muted-foreground">Trusted by 10,000+ borrowers</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-5xl md:text-7xl lg:text-8xl font-bold tracking-tight mb-6 animate-fade-up" style={{ animationDelay: '0.2s' }}>
          <span className="block text-foreground">Manage Your</span>
          <span className="gold-text">Loans Smarter</span>
        </h1>

        {/* Subheadline */}
        <p className="text-lg md:text-xl text-muted-foreground max-w-2xl mx-auto mb-10 animate-fade-up" style={{ animationDelay: '0.3s' }}>
          Take control of your financial future. Track payments, reduce debt, and achieve financial freedom with our intelligent loan management platform.
        </p>

        {/* CTAs */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 animate-fade-up" style={{ animationDelay: '0.4s' }}>
          <Button 
            size="lg" 
            className="gold-gradient text-primary-foreground font-semibold px-8 py-6 text-lg hover-glow group"
          >
            Get Started Free
            <ArrowRight className="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </Button>
          <Button 
            variant="outline" 
            size="lg" 
            className="border-primary/50 text-foreground hover:bg-primary/10 px-8 py-6 text-lg"
            onClick={scrollToFeatures}
          >
            Learn More
          </Button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-8 mt-16 pt-16 border-t border-border/30 animate-fade-up" style={{ animationDelay: '0.5s' }}>
          <div className="text-center">
            <div className="text-3xl md:text-4xl font-bold gold-text">$2.5B+</div>
            <div className="text-sm text-muted-foreground mt-1">Loans Managed</div>
          </div>
          <div className="text-center">
            <div className="text-3xl md:text-4xl font-bold gold-text">50K+</div>
            <div className="text-sm text-muted-foreground mt-1">Active Users</div>
          </div>
          <div className="text-center">
            <div className="text-3xl md:text-4xl font-bold gold-text">4.9★</div>
            <div className="text-sm text-muted-foreground mt-1">User Rating</div>
          </div>
        </div>
      </div>

      {/* Scroll indicator */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
        <ChevronDown className="w-8 h-8 text-primary/60" />
      </div>
    </section>
  );
}
