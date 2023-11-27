import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;


public class BateauxPecheurs extends JFrame {
    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT = 600;
    private final int TIMER_DELAY = 20;
    private int timerDelay = TIMER_DELAY;  // Instance variable
    private final int NEW_FISH_DELAY = 7000;
    private final int MAX_FISH_COUNT = 20;
    private final int BATEAU_WIDTH = 40;
    private final int BATEAU_HEIGHT = 20;
    private Timer fishTimer;
    private Point[] pointsDePeche = new Point[6];
    private int poissonsPeche = 0;
    private Random random = new Random();
    private Image bateauPeche;
    private Image bateauPecheAvecCanne;
    private Image poisson;
    private final int FISH_DIAMETER = 10;
    private JSlider sliderVitesse;



    class BateauPecheur {
        Point position;
        Vector direction;
        int tempsRestantPourPeche = 0;
        int cible = -1;

        public BateauPecheur(Point position, Vector direction) {
            this.position = position;
            this.direction = direction;
        }
    }

    private BateauPecheur[] bateauxPecheurs = {
            new BateauPecheur(new Point(100, 300), getRandomDirection()),
            new BateauPecheur(new Point(600, 500), getRandomDirection()),
            new BateauPecheur(new Point(500, 200), getRandomDirection()),
            new BateauPecheur(new Point(250, 400), getRandomDirection()),
            new BateauPecheur(new Point(950, 590), getRandomDirection()),
            new BateauPecheur(new Point(550, 210), getRandomDirection()),
            new BateauPecheur(new Point(290, 660), getRandomDirection())
    };

    private Vector getRandomDirection() {
        Vector dir = new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
        dir.normalize();
        return dir;
    }

    class Vector {
        public double x;
        public double y;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void normalize() {
            double length = Math.sqrt(x * x + y * y);
            if (length != 0) {
                x /= length;
                y /= length;
            }
        }
    }
    private Timer simulationTimer;


    public BateauxPecheurs() {
        ImageIcon bateauIcon = new ImageIcon("bateauPeche.png");
        bateauPeche = bateauIcon.getImage();
        ImageIcon bateauAvecCanneIcon = new ImageIcon("bateauPecheAvecCanne.png");
        bateauPecheAvecCanne = bateauAvecCanneIcon.getImage();
        ImageIcon poissonIcon = new ImageIcon("poisson.png");
        poisson = poissonIcon.getImage();

        // Création du slider pour contrôler la vitesse de simulation
        sliderVitesse = new JSlider(0, 1000, 0);  // Initial value is set to 0
        sliderVitesse.setMajorTickSpacing(500);
        sliderVitesse.setMinorTickSpacing(250);
        sliderVitesse.setPaintTicks(true);
        sliderVitesse.setPaintLabels(true);
        sliderVitesse.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = sliderVitesse.getValue();
                int delay = (int) (TIMER_DELAY / (1 + value / 100.0));
                simulationTimer.setDelay(delay);
                fishTimer.setDelay(delay);
            }
        });



            // Ajoutez le slider à un nouveau panel et ce panel à la fenêtre principale
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        sliderPanel.add(new JLabel("Vitesse de simulation:"));
        sliderPanel.add(sliderVitesse);
        this.add(sliderPanel, BorderLayout.SOUTH);


        for (int i = 0; i < pointsDePeche.length; i++) {
            pointsDePeche[i] = new Point(random.nextInt(WINDOW_WIDTH - 10) + 5, random.nextInt(WINDOW_HEIGHT - 10) + 5);
        }

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("Bateaux Pêcheurs");

        fishTimer = new Timer(NEW_FISH_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 3; i++) {
                    if (pointsDePeche.length < MAX_FISH_COUNT) {
                        Point newFish = new Point(
                                random.nextInt(WINDOW_WIDTH - 100) + 50,
                                random.nextInt(WINDOW_HEIGHT - 100) + 50);

                        pointsDePeche = Arrays.copyOf(pointsDePeche, pointsDePeche.length + 1);
                        pointsDePeche[pointsDePeche.length - 1] = newFish;
                    }
                }
            }
        });
        fishTimer.start();

        simulationTimer = new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (BateauPecheur bateauPecheur : bateauxPecheurs) {
                    Point coinBasDroitBateau = new Point(bateauPecheur.position.x + BATEAU_WIDTH, bateauPecheur.position.y + BATEAU_HEIGHT);

                    gererCollisions(bateauPecheur);
                    eviterAutresBateaux(bateauPecheur);

                    int cible = getPointCibleLePlusProche(coinBasDroitBateau, bateauPecheur);

                    if (cible == -1) {
                        if (bateauEnDangerDeCollision(bateauPecheur.position)) {
                            ajusterDirection(bateauPecheur);
                        }

                        bateauPecheur.position.x += bateauPecheur.direction.x * 1.05; //vitesse quand pas de cible
                        bateauPecheur.position.y += bateauPecheur.direction.y * 1.05;
                        if (bateauPecheur.position.x < 0 || bateauPecheur.position.x > WINDOW_WIDTH - BATEAU_WIDTH ||
                                bateauPecheur.position.y < 0 || bateauPecheur.position.y > WINDOW_HEIGHT - BATEAU_HEIGHT) {
                            ajusterDirection(bateauPecheur);
                        }

                        continue;
                    }
                    Point pointCible = pointsDePeche[cible];
                    if (bateauPecheur.tempsRestantPourPeche > 0) {
                        bateauPecheur.tempsRestantPourPeche -= TIMER_DELAY;
                        if (bateauPecheur.tempsRestantPourPeche <= 0) {
                            pointCible.setLocation(-10, -10);
                            bateauPecheur.cible = -1;
                            bateauPecheur.tempsRestantPourPeche = 0;
                            poissonsPeche++;
                        }
                    } else if (coinBasDroitBateau.distance(pointCible) < 0.5) {
                        bateauPecheur.tempsRestantPourPeche = 5000;
                        bateauPecheur.cible = cible;
                    } else {
                        int dx = pointCible.x - (bateauPecheur.position.x + BATEAU_WIDTH);
                        int dy = pointCible.y - (bateauPecheur.position.y + BATEAU_HEIGHT);
                        double distance = coinBasDroitBateau.distance(pointCible);
                        bateauPecheur.position.x += (int) (dx / distance * 2);
                        bateauPecheur.position.y += (int) (dy / distance * 2);
                    }

                }
                repaint();
            }
        });
        simulationTimer.start();

        add(new DrawingPanel());
        setVisible(true);
    }
    private boolean bateauEnDangerDeCollision(Point bateauPosition) {
        for (BateauPecheur autreBateauPecheur : bateauxPecheurs) {
            Point autreBateauPosition = autreBateauPecheur.position;
            Rectangle bateauRect = new Rectangle(bateauPosition.x, bateauPosition.y, BATEAU_WIDTH, BATEAU_HEIGHT);
            Rectangle autreBateauRect = new Rectangle(autreBateauPosition.x, autreBateauPosition.y, BATEAU_WIDTH, BATEAU_HEIGHT);

            if (!bateauPosition.equals(autreBateauPosition) && bateauRect.intersects(autreBateauRect)) {
                return true;
            }
        }
        return false;
    }

    private void ajusterDirection(BateauPecheur bateauPecheur) {
        for (int tries = 0; tries < 10; tries++) {
            Vector newDirection = getRandomDirection();
            Point predictedPosition = new Point((int) (bateauPecheur.position.x + newDirection.x * 5),
                    (int) (bateauPecheur.position.y + newDirection.y * 5));

            if (!bateauEnDangerDeCollision(predictedPosition)) {
                bateauPecheur.direction = newDirection;
                return;
            }
        }
    }
    private void eviterAutresBateaux(BateauPecheur bateauPecheur) {
        Point bateauPosition = bateauPecheur.position;
        Rectangle bateauRect = new Rectangle(bateauPosition.x, bateauPosition.y, BATEAU_WIDTH, BATEAU_HEIGHT);

        double champVision = 100; // Distance devant le bateau pour détecter d'autres bateaux

        for (BateauPecheur autreBateauPecheur : bateauxPecheurs) {
            if (bateauPecheur == autreBateauPecheur) continue; // Ne pas comparer le bateau avec lui-même
            Point autreBateauPosition = autreBateauPecheur.position;
            Rectangle autreBateauRect = new Rectangle(autreBateauPosition.x, autreBateauPosition.y, BATEAU_WIDTH, BATEAU_HEIGHT);

            double dx = autreBateauPosition.x - bateauPosition.x;
            double dy = autreBateauPosition.y - bateauPosition.y;
            double distance = Math.sqrt(dx*dx + dy*dy);

            if (distance < champVision && bateauRect.intersects(autreBateauRect)) {
                double angleDodge = Math.atan2(dy, dx) + (random.nextBoolean() ? -1 : 1) * Math.PI / 2;
                double push = champVision - distance;
                bateauPecheur.direction.x += push * Math.cos(angleDodge) / distance;
                bateauPecheur.direction.y += push * Math.sin(angleDodge) / distance;
            }
        }
        // Normalize la direction pour garantir une vitesse constante
        bateauPecheur.direction.normalize();
    }



    private void gererCollisions(BateauPecheur bateauPecheur) {
        int compteBateauxProches = 0;
        Point bateauPosition = bateauPecheur.position;

        for (BateauPecheur autreBateauPecheur : bateauxPecheurs) {
            Point autreBateauPosition = autreBateauPecheur.position;
            if (!bateauPosition.equals(autreBateauPosition) && bateauPecheur.cible == -1 && bateauPosition.distance(autreBateauPosition) < 55) {
                compteBateauxProches++;
            }
        }

        for (BateauPecheur autreBateauPecheur : bateauxPecheurs) {
            Point autreBateauPosition = autreBateauPecheur.position;
            if (!bateauPosition.equals(autreBateauPosition) && bateauPosition.distance(autreBateauPosition) < 55) {

                if (bateauPecheur.cible == -1 && compteBateauxProches > 1) {
                    double ralentissement = 0.5;
                    bateauPosition.x += bateauPecheur.direction.x * ralentissement;
                    bateauPosition.y += bateauPecheur.direction.y * ralentissement;
                }

                boolean ceBateauPeche = bateauPecheur.tempsRestantPourPeche > 0;
                boolean autreBateauPeche = autreBateauPecheur.tempsRestantPourPeche > 0;

                if (bateauPecheur.cible == -1) {
                    if (autreBateauPeche) {
                        double angle = Math.atan2(autreBateauPosition.y - bateauPosition.y, autreBateauPosition.x - bateauPosition.x) + Math.PI / 2;
                        int avoidanceSpeed = 1;
                        bateauPosition.x += avoidanceSpeed * Math.cos(angle);
                        bateauPosition.y += avoidanceSpeed * Math.sin(angle);
                    } else if (compteBateauxProches > 1 && random.nextInt(compteBateauxProches) > 0) {
                        double angle = Math.atan2(autreBateauPosition.y - bateauPosition.y, autreBateauPosition.x - bateauPosition.x) + Math.PI / 2;
                        int avoidanceSpeed = 1;
                        bateauPosition.x += avoidanceSpeed * Math.cos(angle);
                        bateauPosition.y += avoidanceSpeed * Math.sin(angle);
                    }
                }

                if (bateauPosition.distance(autreBateauPosition) < 30) {
                    bateauPosition.setLocation(-100, -100);
                    autreBateauPosition.setLocation(-100, -100);
                }
            }
        }
    }

    private int getPointCibleLePlusProche(Point coinBasDroitBateau, BateauPecheur bateauPecheur) {
        int cible = -1;
        double distanceMin = Double.MAX_VALUE;

        for (int i = 0; i < pointsDePeche.length; i++) {
            double distance = coinBasDroitBateau.distance(pointsDePeche[i]);

            boolean autreBateauPlusProche = false;
            for (BateauPecheur autreBateauPecheur : bateauxPecheurs) {
                Point autreBateauPosition = autreBateauPecheur.position;
                if (autreBateauPecheur != bateauPecheur && autreBateauPosition.distance(pointsDePeche[i]) < distance) {
                    autreBateauPlusProche = true;
                    break;
                }
            }

            if (!autreBateauPlusProche && distance < distanceMin && (bateauPecheur.cible == -1 || bateauPecheur.cible == i)) {
                distanceMin = distance;
                cible = i;
            }
        }
        return cible;
    }

    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

            for (BateauPecheur bateauPecheur : bateauxPecheurs) {
                g.drawImage(bateauPeche, bateauPecheur.position.x, bateauPecheur.position.y + 5, BATEAU_WIDTH, BATEAU_HEIGHT, this);
            }

            for (Point point : pointsDePeche) {
                g.drawImage(poisson, point.x - FISH_DIAMETER , point.y - FISH_DIAMETER , FISH_DIAMETER, FISH_DIAMETER, this);
            }



            // Affichage du nombre total de poissons pêchés
            String infoPoissons = "Poissons pêchés : " + poissonsPeche;

            int textWidth = g.getFontMetrics().stringWidth(infoPoissons) + 45;
            int textHeight = g.getFontMetrics().getHeight() + 4;

            // Fond sombre pour le texte
            g.setColor(new Color(0, 0, 0, 127));
            g.fillRect(10, 10, textWidth, textHeight);

            // Texte
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(infoPoissons, 15, 25);
        }
    }


    public static void main(String[] args) {
        new BateauxPecheurs();
    }
}
