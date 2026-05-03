// ============================================================================
// PAGE FRONT-END : CATÉGORIE FORUM
// Affiche la liste des forums d'une catégorie spécifique
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import 'widgets/common_widgets.dart';
import 'forum_detail_page.dart';

class CategoryPage extends StatefulWidget {
  final Category category;

  const CategoryPage({Key? key, required this.category}) : super(key: key);

  @override
  _CategoryPageState createState() => _CategoryPageState();
}

class _CategoryPageState extends State<CategoryPage> {
  late ApiService api;
  List<Forum> forums = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadForums();
  }

  Future<void> _loadForums() async {
    setState(() => isLoading = true);
    try {
      final forumsData = await api.getForums(categoryId: widget.category.id);
      setState(() {
        forums = forumsData;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showErrorSnackBar('Erreur lors du chargement des forums');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      body: Row(
        children: [
          _buildSidebar(),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(),
                Expanded(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Breadcrumb
                        _buildBreadcrumb(),
                        const SizedBox(height: 24),

                        // En-tête coloré
                        _buildCategoryHeader(),
                        const SizedBox(height: 24),

                        // Badges
                        _buildStatsBadges(),
                        const SizedBox(height: 24),

                        // Liste des forums
                        isLoading
                            ? const Center(child: CircularProgressIndicator())
                            : _buildForumsList(),
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
          _buildNavIcon(Icons.home, false),
          _buildNavIcon(Icons.person, false),
          _buildNavIcon(Icons.book, false),
          _buildNavIcon(Icons.map, false),
          _buildNavIcon(Icons.video_call, false),
          _buildNavIcon(Icons.sports_esports, false),
          _buildNavIcon(Icons.event, false),
          _buildNavIcon(Icons.forum, true),
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

  Widget _buildBreadcrumb() {
    return Row(
      children: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text(
            "Forums",
            style: TextStyle(color: Colors.grey, fontSize: 14),
          ),
        ),
        const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
        Text(
          widget.category.title,
          style: const TextStyle(
            color: Colors.green,
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }

  Widget _buildCategoryHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: const BoxDecoration(
        color: Colors.green,
        borderRadius: BorderRadius.all(Radius.circular(12)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.tag, color: Colors.white.withOpacity(0.8), size: 32),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  widget.category.title,
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            widget.category.description,
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsBadges() {
    return Row(
      children: [
        _buildStatBadge(
          "${forums.length} forum${forums.length > 1 ? 's' : ''}",
          Icons.forum,
          Colors.green,
        ),
        const SizedBox(width: 16),
        _buildStatBadge(
          "${forums.fold<int>(0, (sum, f) => sum + f.messageCount)} messages",
          Icons.chat,
          Colors.blue,
        ),
      ],
    );
  }

  Widget _buildStatBadge(String label, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: color),
          const SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w500,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildForumsList() {
    if (forums.isEmpty) {
      return const Center(
        child: Text(
          "Aucun forum dans cette catégorie",
          style: TextStyle(fontSize: 16, color: Colors.grey),
        ),
      );
    }

    return Column(
      children: forums.map((forum) {
        return _buildForumCard(forum);
      }).toList(),
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
              "Voir →",
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
