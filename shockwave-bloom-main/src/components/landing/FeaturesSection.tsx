import { BarChart3, Calculator, Bell, TrendingUp } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

const features = [
  {
    icon: BarChart3,
    title: "Loan Dashboard",
    description: "Track all your loans in one centralized dashboard. Get a complete overview of balances, rates, and payment schedules at a glance.",
  },
  {
    icon: Calculator,
    title: "Payment Calculator",
    description: "Plan your payments with precision. See how extra payments accelerate your debt-free journey and save on interest.",
  },
  {
    icon: Bell,
    title: "Smart Reminders",
    description: "Never miss a payment. Receive intelligent notifications before due dates and stay on top of your financial commitments.",
  },
  {
    icon: TrendingUp,
    title: "Progress Tracking",
    description: "Watch your debt decrease over time. Visualize your progress with beautiful charts and celebrate every milestone.",
  },
];

export default function FeaturesSection() {
  return (
    <section id="features" className="py-24 px-4 relative">
      <div className="max-w-6xl mx-auto">
        {/* Section Header */}
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-5xl font-bold mb-4">
            <span className="text-foreground">Everything You Need to</span>
            <br />
            <span className="gold-text">Conquer Your Debt</span>
          </h2>
          <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
            Powerful tools designed specifically for individual borrowers to take control of their financial journey.
          </p>
        </div>

        {/* Features Grid */}
        <div className="grid md:grid-cols-2 gap-6">
          {features.map((feature, index) => (
            <Card 
              key={feature.title}
              className="glass-card border-border/30 hover-glow group cursor-pointer transition-all duration-300 hover:border-primary/50 overflow-hidden"
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <CardContent className="p-8">
                <div className="flex items-start gap-5">
                  <div className="p-3 rounded-xl gold-gradient shrink-0 group-hover:scale-110 transition-transform duration-300">
                    <feature.icon className="w-6 h-6 text-primary-foreground" />
                  </div>
                  <div>
                    <h3 className="text-xl font-semibold text-foreground mb-2 group-hover:text-primary transition-colors">
                      {feature.title}
                    </h3>
                    <p className="text-muted-foreground leading-relaxed">
                      {feature.description}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
