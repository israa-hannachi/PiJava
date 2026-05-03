// ============================================================================
// PAGE FRONT-END : LISTE PRINCIPALE DES FORUMS
// Interface Utilisateur - Page d'accueil Forums
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import 'category_page.dart';
import 'widgets/common_widgets.dart'; // Navigation latérale, header, etc.

class ForumsPage extends StatefulWidget {
  const ForumsPage({Key? key}) : super(key: key);

  @override
  _ForumsPageState createState() => _ForumsPageState();
}

class _ForumsPageState extends State<ForumsPage> {
  late ApiService api;
  List<Category> categories = [];
  List<Forum> userForums = [];
  int totalUserMessages = 0;
  bool isLoading = true;
  String searchQuery = '';

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => isLoading = true);

    try {
      // Charger les catégories
      final categoriesData = await api.getCategories();
      // Charger les forums de l'utilisateur (user_id = 1 pour test)
      final userForumsData = await api.getForums(userId: 1);

      setState(() {
        categories = categoriesData;
        userForums = userForumsData;
        totalUserMessages = userForumsData.fold<int>(
            0, (sum, forum) => sum + forum.messageCount);
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showErrorSnackBar('Erreur lors du chargement des données');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      // ==================== HEADER & NAVIGATION LATÉRALE ====================
      body: Row(
        children: [
          // Sidebar gauche (icônes navigation)
          _buildSidebar(),

          // Zone principale
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
                        // Titre & sous-titre
                        _buildTitleSection(),

                        const SizedBox(height: 24),

                        // Bloc "Votre Activité"
                        _buildActivityBlock(),

                        const SizedBox(height: 24),

                        // Barre de recherche
                        _buildSearchBar(),

                        const SizedBox(height: 32),

                        // Liste des catégories
                        _buildCategoriesSection(),

                        const SizedBox(height: 32),

                        // Titre "Vos Forums Récents"
                        if (userForums.isNotEmpty) ...[
                          const Text(
                            "Vos Forums Récents",
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: Colors.black87,
                            ),
                          ),
                          const SizedBox(height: 16),
                          _buildUserForumsList(),
                        ],
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

  Widget _buildSidebar() {
    return Container(
      width: 70,
      color: Colors.white,
      child: Column(
        children: [
          const SizedBox(height: 20),
          // Logo "naja7ni"
          Container(
            width: 40,
            height: 40,
            decoration: const BoxDecoration(
              color: Colors.green,
              shape: BoxShape.circle,
            ),
            child: const Center(
              child: Text(
                "N",
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ),
              ),
            ),
          ),
          const SizedBox(height: 30),
          // Icônes navigation
          _buildNavIcon(Icons.home, false),
          _buildNavIcon(Icons.person, false),
          _buildNavIcon(Icons.book, false),
          _buildNavIcon(Icons.map, false),
          _buildNavIcon(Icons.video_call, false),
          _buildNavIcon(Icons.sports_esports, false),
          _buildNavIcon(Icons.event, false),
          _buildNavIcon(Icons.forum, true), // Forums - actif
        ],
      ),
    );
  }

  Widget _buildNavIcon(IconData icon, bool isActive) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Icon(
        icon,
        color: isActive ? Colors.green : Colors.grey,
        size: 28,
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      color: Colors.white,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Logo texte
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
          // Actions utilisateur
          Row(
            children: [
              IconButton(
                icon: const Icon(Icons.notifications_border, color: Colors.grey),
                onPressed: () {},
              ),
              IconButton(
                icon: const Icon(Icons.message_outlined, color: Colors.grey),
                onPressed: () {},
              ),
              const SizedBox(width: 12),
              Container(
                width: 40,
                height: 40,
                decoration: const BoxDecoration(
                  color: Colors.green,
                  shape: BoxShape.circle,
                ),
                child: const Center(
                  child: Text(
                    "U",
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTitleSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: const [
        Text(
          "Forums de Discussion",
          style: TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        SizedBox(height: 8),
        Text(
          "Échangez avec la communauté",
          style: TextStyle(
            fontSize: 16,
            color: Colors.grey,
          ),
        ),
      ],
    );
  }

  Widget _buildActivityBlock() {
    return Row(
      children: [
        Expanded(
          child: _buildActivityCard(
            "Vos Forums",
            userForums.length.toString(),
            Colors.green,
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: _buildActivityCard(
            "Messages",
            totalUserMessages.toString(),
            Colors.blue,
          ),
        ),
      ],
    );
  }

  Widget _buildActivityCard(String title, String count, Color color) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            count,
            style: TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchBar() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: TextField(
        decoration: InputDecoration(
          hintText: "Rechercher dans les forums...",
          prefixIcon: const Icon(Icons.search, color: Colors.grey),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        ),
        onChanged: (value) {
          setState(() => searchQuery = value.toLowerCase());
        },
      ),
    );
  }

  Widget _buildCategoriesSection() {
    final filteredCategories = categories.where((category) {
      if (searchQuery.isEmpty) return true;
      return category.title.toLowerCase().contains(searchQuery) ||
          category.description.toLowerCase().contains(searchQuery);
    }).toList();

    if (filteredCategories.isEmpty) {
      return const Center(
        child: Text(
          "Aucune catégorie trouvée",
          style: TextStyle(fontSize: 16, color: Colors.grey),
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Catégories de Forums",
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        const SizedBox(height: 16),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 16,
            mainAxisSpacing: 16,
            childAspectRatio: 1.2,
          ),
          itemCount: filteredCategories.length,
          itemBuilder: (context, index) {
            final category = filteredCategories[index];
            return _buildCategoryCard(category);
          },
        ),
      ],
    );
  }

  Widget _buildCategoryCard(Category category) {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => CategoryPage(category: category),
          ),
        );
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.1),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                // Icône ronde colorée
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: category.color.withOpacity(0.2),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    Icons.tag,
                    color: category.color,
                    size: 24,
                  ),
                ),
                const Spacer(),
                // Flèche droite
                Icon(Icons.arrow_forward_ios, color: Colors.grey[400], size: 16),
              ],
            ),
            const SizedBox(height: 12),
            // Titre
            Text(
              category.title,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            // Description
            Text(
              category.description,
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey[600],
              ),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const Spacer(),
            // Nombre de forums
            Text(
              "${category.forumCount} forum${category.forumCount > 1 ? 's' : ''}",
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey[500],
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildUserForumsList() {
    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: userForums.length,
      itemBuilder: (context, index) {
        final forum = userForums[index];
        return _buildForumCard(forum);
      },
    );
  }

  Widget _buildForumCard(Forum forum) {
    final dateFormat = "${forum.createdAt.day}/${forum.createdAt.month}/${forum.createdAt.year}";

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
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
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        forum.title,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Colors.black87,
                        ),
                      ),
                    ),
                    if (forum.levelTag != null)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.purple.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          forum.levelTag!,
                          style: const TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            color: Colors.purple,
                          ),
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  forum.description,
                  style: TextStyle(
                    fontSize: 13,
                    color: Colors.grey[600],
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    // Auteur
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.green.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        forum.author?.name ?? "Utilisateur",
                        style: const TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w500,
                          color: Colors.green,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      dateFormat,
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.grey[500],
                      ),
                    ),
                    const Spacer(),
                    // Compteur messages
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.chat_bubble_outline, size: 14, color: Colors.grey),
                        const SizedBox(width: 4),
                        Text(
                          forum.messageCount.toString(),
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          ),
          // Bouton "Voir"
          TextButton(
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ForumDetailPage(forum: forum),
                ),
              );
            },
            child: const Text(
              "Voir",
              style: TextStyle(
                color: Colors.green,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
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
}
