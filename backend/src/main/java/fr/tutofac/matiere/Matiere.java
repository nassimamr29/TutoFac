package fr.tutofac.matiere;

import jakarta.persistence.*;

@Entity
@Table(name = "matiere")
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String intitule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauEtudes niveau;

    @Column(nullable = false)
    private String domaine;

    public Long getId() { return id; }
    public String getIntitule() { return intitule; }
    public NiveauEtudes getNiveau() { return niveau; }
    public String getDomaine() { return domaine; }
}
