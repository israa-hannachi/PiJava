// ============================================================================
// PAGE BACK-END : DASHBOARD FORUMS (ADMIN)
// Vue d'ensemble des statistiques et actions rapides
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import 'widgets/admin_widgets.dart';
import 'admin_pages/create_forum_page.dart';
import 'admin_pages/category_form_page.dart';
import 'admin_pages/statistics_page.dart';
import 'admin_pages/clustering_page.dart';
import '../screens/frontend/forums_page.dart';

class AdminDashboardPage extends StatefulWidget {
  const AdminDashboardPage({Key? key}) : super(key: key);

  @override
  _AdminDashboardPageState createState() => _AdminDashboardPageState();
}

class _AdminDashboardPageState extends State<AdminDashboardPage> {
  late ApiService api;
  ForumStats? stats;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadStats();
  }

  Future<void> _loadStats() async {
    setState(() => isLoading = true);
    try {
      final statsData = await api.getForumStats();
      setState(() {
        stats = statsData;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showErrorSnackBar('Erreur lors du chargement des statistiques');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      body: Row(
        children: [
          // Sidebar admin
          AdminSidebar(
            activeIndex: 0,
            onItemTapped: (index) => _handleNavigation(index),
          ),

          // Contenu principal
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header
                _buildHeader(),
                // Contenu scrollable
                Expanded(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Titre
                        const Text(
                          "NAJA7NI — Dashboard Forums",
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 8),
                        const Text(
                          "Vue d'ensemble de l'activité des forums",
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.grey,
                          ),
                        ),
                        const SizedBox(height: 24),

                        // 4 Cartes métriques
                        isLoading
                            ? const Center(child: CircularProgressIndicator())
                            : _buildMetricsCards(),

                        const SizedBox(height: 32),

                        // Actions Rapides
                        _buildQuickActions(),

                        const SizedBox(height: 32),

                        // Section Gestion
                        _buildManagementSection(),

                        const SizedBox(height: 32),

                        // Activité récente (optionnel)
                        _buildRecentActivity(),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ==================== WIDGETS UI ====================

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      color: Colors.white,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              IconButton(
                icon: const Icon(Icons.menu),
                onPressed: () {
                  // Toggle sidebar mobile
                },
              ),
              const SizedBox(width: 8),
              Row(
                children: const [
                  Icon(Icons.school, color: Colors.green, size: 32),
                  SizedBox(width: 10),
                  Text(
                    "naja7ni",
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.green,
                    ),
                  ),
                ],
              ),
            ],
          ),
          Row(
            children: [
              TextButton(
                onPressed: () {},
                child: const Text(
                  "Dashboard",
                  style: TextStyle(color: Colors.black87),
                ),
              ),
              const SizedBox(width: 8),
              ElevatedButton.icon(
                onPressed: () async {
                  await AuthService().logout();
                },
                icon: const Icon(Icons.logout, size: 18),
                label: const Text("Logout"),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMetricsCards() {
    if (stats == null) return const SizedBox.shrink();

    return Row(
      children: [
        MetricCard(
          title: "Catégories",
          value: stats!.totalCategories.toString(),
          color: Colors.blue,
          icon: Icons.category,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Forums",
          value: stats!.totalForums.toString(),
          color: Colors.green,
          icon: Icons.forum,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Messages",
          value: stats!.totalMessages.toString(),
          color: Colors.orange,
          icon: Icons.chat,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Forums Actifs",
          value: stats!.activeForums.toString(),
          color: Colors.purple,
          icon: Icons.check_circle,
        ),
      ],
    );
  }

  Widget _buildQuickActions() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Actions Rapides",
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        const SizedBox(height: 16),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            ActionButton(
              label: "Créer un nouveau forum",
              icon: Icons.add_forum,
              color: Colors.green,
              onPressed: () => _navigateTo(CreateForumPage()),
            ),
            ActionButton(
              label: "Ajouter une catégorie",
              icon: Icons.category_add,
              color: Colors.blue,
              onPressed: () => _navigateTo(CategoryFormPage()),
            ),
            ActionButton(
              label: "Statistiques",
              icon: Icons.bar_chart,
              color: Colors.orange,
              onPressed: () => _navigateTo(StatisticsPage()),
            ),
            ActionButton(
              label: "Rapport résumé",
              icon: Icons.assessment,
              color: Colors.red,
              onPressed: () => _showReportDialog(),
            ),
            ActionButton(
              label: "Clustering des discussions",
              icon: Icons.hub,
              color: Colors.purple,
              onPressed: () => _navigateTo(ClusteringPage()),
            ),
            ActionButton(
              label: "Go to Front",
              icon: Icons.arrow_forward,
              color: Colors.teal,
              isOutlined: true,
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ForumsPage()),
                );
              },
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildManagementSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Gestion",
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        const SizedBox(height: 16),
        Row(
          children: [
            Expanded(
              child: _buildManagementCard(
                "Gérer Catégories",
                Icons.category,
                Colors.blue,
                () => _navigateTo(CategoryFormPage()),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _buildManagementCard(
                "Gérer Forums",
                Icons.forum,
                Colors.green,
                () => _showForumsManagement(),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _buildManagementCard(
                "Gérer Messages",
                Icons.chat,
                Colors.orange,
                () => _showMessagesManagement(),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildManagementCard(
      String title, IconData icon, Color color, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.1),
              blurRadius: 6,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          children: [
            Icon(icon, size: 40, color: color),
            const SizedBox(height: 12),
            Text(
              title,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: Colors.black87,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecentActivity() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Activité Récente",
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 12),
          // TODO: Afficher l'activité récente depuis l'API
          const Text(
            "Aucune activité récente à afficher",
            style: TextStyle(color: Colors.grey),
          ),
        ],
      ),
    );
  }

  void _handleNavigation(int index) {
    switch (index) {
      case 0:
        // Already on Dashboard
        break;
      case 1:
        // Comptes
        break;
      case 2:
        // Cours
        break;
      case 3:
        // Jeux
        break;
      case 4:
        // Forums -> Liste des forums
        _navigateTo(CreateForumPage());
        break;
      case 7:
        // Mailing
        break;
      case 8:
        // Retour Front
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const ForumsPage()),
        );
        break;
      case 9:
        // Logout
        AuthService().logout();
        break;
    }
  }

  void _navigateTo(Widget page) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => page),
    ).then((_) => _loadStats());
  }

  void _showReportDialog() {
    showDialog(
      context: context,
      builder: (context) => const ExportModal(),
    );
  }

  void _showForumsManagement() {
    // TODO: Implement forums management table view
    _showInfoSnackBar("Gestion des forums - À implémenter");
  }

  void _showMessagesManagement() {
    // TODO: Implement messages management table view
    _showInfoSnackBar("Gestion des messages - À implémenter");
  }

  void _showSuccessSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.green,
      ),
    );
  }

  void _showErrorSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
      ),
    );
  }

  void _showInfoSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.blue,
      ),
    );
  }
}
