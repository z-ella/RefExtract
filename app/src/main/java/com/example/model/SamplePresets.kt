package com.example.model

data class SamplePreset(
    val id: String,
    val title: String,
    val category: String,
    val text: String
)

object SamplePresets {
    val allPresets = listOf(
        SamplePreset(
            id = "academic",
            title = "Academic Paper (AI & Neuroscience)",
            category = "Research",
            text = """In recent breakthroughs published by Vaswani et al. (2017), transformer architectures achieved 94.2% top-1 benchmark accuracy across multiple machine translation benchmarks. Subsequent evaluations by Kaplan et al. (2020) demonstrated empirical scaling laws across 100 billion parameters. As Dr. Demis Hassabis remarked at the Royal Society conference: "Neural algorithmic reasoning bridges classical symbolic systems with connectionist models." Further investigations published in Nature (DOI: 10.1038/s41586-021-03819-2) and indexed under arXiv:2106.09685 confirm that graph neural networks accelerate molecular dynamics by 150x. Research teams at DeepMind in London and Stanford University in California collaborated under IEEE Standard 754-2019 to ensure numerical stability."""
        ),
        SamplePreset(
            id = "tech_news",
            title = "Tech Industry & Leadership Interview",
            category = "News",
            text = """On October 24, 2024, Alphabet CEO Sundar Pichai addressed shareholders in Mountain View, California. "We are seeing generative AI drive over 35% productivity gains in cloud infrastructure workflows," Pichai declared during the annual keynote. The enterprise division recorded ${'$'}9.2 billion in operating revenue, an increase of 28.5% compared to Q3 2023. OpenAI CTO Mira Murati previously noted in Geneva: "Safety validation must precede frontier deployment by at least 6 months." The initiative complies with European Union ISO 27001 certifications and the NIST AI Risk Management Framework 1.0."""
        ),
        SamplePreset(
            id = "legal_compliance",
            title = "Regulatory & Data Privacy Assessment",
            category = "Legal",
            text = """Under Article 17 of the EU General Data Protection Regulation (GDPR), data subjects possess an enforceable 'Right to Erasure'. As Chief Compliance Officer Sarah Jenkins stated in her brief: "Failure to automate compliance audits creates an unacceptable liability threshold." Cross-border data transfers from Tokyo to New York must satisfy standard contractual clauses (SCCs) outlined in Directive 95/46/EC and comply with ISO/IEC 27701:2019. Penalties may reach up to €20 million or 4.0% of worldwide annual turnover, as cited in Case C-311/18 (Schrems II)."""
        ),
        SamplePreset(
            id = "biomedical",
            title = "Clinical Trial & Pharmacology Report",
            category = "Medical",
            text = """A Phase 3 double-blind randomized clinical trial (ClinicalTrials.gov identifier NCT04368728) evaluated the efficacy of monoclonal antibody treatment across 4,500 patients aged 18 to 75. Principal Investigator Dr. Elena Rostova commented: "The therapeutic response demonstrated a 68.3% reduction in hospitalization risk within 72 hours." Findings documented in The Lancet (DOI: 10.1016/S0140-6736(21)00425-4) highlight that adverse events occurred in less than 1.2% of participants receiving 250 mg daily doses."""
        )
    )
}
