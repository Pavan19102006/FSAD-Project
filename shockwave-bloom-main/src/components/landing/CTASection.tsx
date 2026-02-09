import { Button } from "@/components/ui/button";
import { ArrowRight, Shield, Users, Award } from "lucide-react";

const trustIndicators = [
  { icon: Shield, text: "Bank-level Security" },
  { icon: Users, text: "50,000+ Users" },
  { icon: Award, text: "Top Rated App" },
];

export default function CTASection() {
  return (
    <section className="py-24 px-4 relative">
      <div className="max-w-4xl mx-auto">
        {/* Main CTA Card */}
        <div className="glass-card rounded-3xl p-8 md:p-12 text-center border border-primary/30 relative overflow-hidden">
          {/* Background glow */}
          <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-bronze/10 pointer-events-none" />
          
          <div className="relative z-10">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              <span className="text-foreground">Start Managing Your</span>
              <br />
              <span className="gold-text">Loans Today</span>
            </h2>
            
            <p className="text-muted-foreground text-lg max-w-xl mx-auto mb-8">
              Join thousands of borrowers who have transformed their financial lives. 
              It's free to start and takes just 2 minutes to set up.
            </p>

            <Button 
              size="lg" 
              className="gold-gradient text-primary-foreground font-semibold px-10 py-6 text-lg glow-gold hover:scale-105 transition-transform group"
            >
              Get Started Free
              <ArrowRight className="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </Button>

            <p className="text-sm text-muted-foreground mt-4">
              No credit card required • Free forever for basic features
            </p>
          </div>
        </div>

        {/* Trust Indicators */}
        <div className="flex flex-wrap items-center justify-center gap-8 mt-12">
          {trustIndicators.map((item) => (
            <div key={item.text} className="flex items-center gap-2 text-muted-foreground">
              <item.icon className="w-5 h-5 text-primary" />
              <span className="text-sm">{item.text}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
