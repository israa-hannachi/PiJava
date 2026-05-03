import 'package:flutter/material.dart';

class FrontForumScreen extends StatelessWidget {
  const FrontForumScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Row(
        children: [
          // Sidebar
          Container(
            width: 120,
            decoration: const BoxDecoration(
              color: Color(0xFFF7FFFE),
              border: Border(right: BorderSide(color: Color(0x1A0FB5A9), width: 1)),
            ),
            padding: const EdgeInsets.fromLTRB(8, 14, 8, 14),
            child: Column(
              children: [
                // Logo
                Image.asset(
                  'assets/images/logo.png',
                  height: 62,
                ),
                const SizedBox(height: 24),
                // Navigation Buttons
                _buildNavButton(Icons.home, 'Accueil'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.person, 'Profil'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.book, 'Cours'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.explore, 'Parcours'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.videocam, 'Meet'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.sports_esports, 'Jeux'),
                const SizedBox(height: 12),
                _buildNavButton(Icons.event, 'Events'),
                const SizedBox(height: 12),
                // Forums button (active)
                Container(
                  decoration: BoxDecoration(
                    color: const Color(0x1A0FB5A9),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: TextButton(
                    onPressed: () {},
                    style: TextButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      minimumSize: const Size(double.infinity, 0),
                    ),
                    child: const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.forum, color: Color(0xFF0FB5A9), size: 20),
                        SizedBox(width: 8),
                        Text(
                          'Forums',
                          style: TextStyle(
                            color: Color(0xFF0FB5A9),
                            fontWeight: FontWeight.w700,
                            fontSize: 14,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const Spacer(),
              ],
            ),
          ),
          // Main Content
          Expanded(
            child: Container(
              color: const Color(0xFFF8FAFC),
              child: SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.all(40),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Header
                      const Text(
                        'Forums de Discussion',
                        style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF1E293B),
                        ),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Échangez avec la communauté',
                        style: TextStyle(
                          fontSize: 14,
                          color: Color(0xFF64748B),
                        ),
                      ),
                      const SizedBox(height: 25),
                      // Votre Activité
                      const Text(
                        'Votre Activité',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF16A34A),
                        ),
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          // Vos Forums Card
                          Expanded(
                            child: _buildActivityCard(
                              Icons.forum,
                              'Vos Forums',
                              '0',
                              const Color(0xFF1E293B),
                            ),
                          ),
                          const SizedBox(width: 20),
                          // Messages Card
                          Expanded(
                            child: _buildActivityCard(
                              Icons.mail,
                              'Messages',
                              '0',
                              const Color(0xFF1E293B),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 25),
                      // Search Bar
                      TextField(
                        decoration: InputDecoration(
                          prefixIcon: const Icon(Icons.search),
                          hintText: '🔍 Rechercher dans les forums...',
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(25),
                            borderSide: BorderSide.none,
                          ),
                          contentPadding:
                              const EdgeInsets.fromLTRB(20, 12, 20, 12),
                        ),
                      ),
                      const SizedBox(height: 25),
                      // Catégories du Forum
                      const Text(
                        'Catégories du Forum',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF1E293B),
                        ),
                      ),
                      const SizedBox(height: 15),
                      // Placeholder for dynamic categories
                      Container(
                        height: 100,
                        color: Colors.grey[200],
                        child: const Center(
                          child: Text('Catégories à charger dynamiquement'),
                        ),
                      ),
                      const SizedBox(height: 25),
                      // Tous les Forums
                      Row(
                        children: [
                          const Text(
                            'Tous les Forums',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: Color(0xFF1E293B),
                            ),
                          ),
                          const Spacer(),
                          DropdownButtonFormField<String>(
                            decoration: InputDecoration(
                              hintText: 'Toutes les catégories',
                              filled: true,
                              fillColor: Colors.white,
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(6),
                                borderSide: BorderSide.none,
                              ),
                              contentPadding:
                                  const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                            items: const [
                              DropdownMenuItem(value: '', child: Text('Toutes les catégories')),
                              DropdownMenuItem(value: 'cat1', child: Text('Catégorie 1')),
                              DropdownMenuItem(value: 'cat2', child: Text('Catégorie 2')),
                            ],
                            onChanged: (_) {},
                          ),
                        ],
                      ),
                      const SizedBox(height: 15),
                      // Placeholder for dynamic forums list
                      Container(
                        height: 200,
                        color: Colors.grey[200],
                        child: const Center(
                          child: Text('Liste des forums à charger dynamiquement'),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      // Floating Chat Button
      floatingActionButton: FloatingActionButton(
        backgroundColor: const Color(0xFF8B5CF6),
        onPressed: () {},
        child: Icon(Icons.chat, color: Colors.white),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.endFloat,
    );
  }

  Widget _buildNavButton(IconData icon, String label) {
    return TextButton.icon(
      onPressed: () {},
      icon: Icon(icon, color: const Color(0xFF64748B), size: 20),
      label: Text(
        label,
        style: const TextStyle(
          color: Color(0xFF64748B),
          fontSize: 14,
        ),
      ),
      style: TextButton.styleFrom(
        padding: const EdgeInsets.symmetric(vertical: 8),
        alignment: Alignment.centerLeft,
      ),
    );
  }

  Widget _buildActivityCard(
      IconData icon, String label, String count, Color countColor) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      padding: const EdgeInsets.all(20),
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontSize: 13,
                  color: Color(0xFF64748B),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                count,
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: countColor,
                ),
              ),
            ],
          ),
          const Spacer(),
          Icon(icon, color: countColor, size: 24),
        ],
      ),
    );
  }
}