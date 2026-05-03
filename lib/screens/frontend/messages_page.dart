// ============================================================================
// PAGE FRONT-END : MESSAGES DU THREAD
// Affiche tous les messages d'un forum avec interactions
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import 'widgets/common_widgets.dart';

class MessagesPage extends StatefulWidget {
  final Forum forum;

  const MessagesPage({Key? key, required this.forum}) : super(key: key);

  @override
  _MessagesPageState createState() => _MessagesPageState();
}

class _MessagesPageState extends State<MessagesPage> {
  late ApiService api;
  late AuthService auth;
  List<Message> messages = [];
  bool isLoading = true;
  int currentPage = 1;
  int totalPages = 1;
  final int messagesPerPage = 10;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    auth = AuthService();
    _loadMessages();
  }

  Future<void> _loadMessages() async {
    setState(() => isLoading = true);
    try {
      final messagesData = await api.getForumMessages(
        widget.forum.id!,
        page: currentPage,
        limit: messagesPerPage,
      );

      setState(() {
        messages = messagesData;
        // Calculer le nombre total de pages (simulé ici)
        totalPages = (widget.forum.messageCount / messagesPerPage).ceil();
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showErrorSnackBar('Erreur lors du chargement des messages');
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

                        // Titre
                        Text(
                          "Messages - ${widget.forum.title}",
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 24),

                        // Liste des messages
                        isLoading
                            ? const Center(child: CircularProgressIndicator())
                            : _buildMessagesList(),

                        // Pagination
                        if (!isLoading && totalPages > 1)
                          _buildPagination(),
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
        if (widget.forum.category != null) ...[
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(
              widget.forum.category!.title,
              style: const TextStyle(color: Colors.grey, fontSize: 14),
            ),
          ),
          const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
        ],
        Text(
          widget.forum.title,
          style: const TextStyle(
            color: Colors.green,
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }

  Widget _buildMessagesList() {
    if (messages.isEmpty) {
      return const Center(
        child: Text(
          "Aucun message dans ce forum",
          style: TextStyle(fontSize: 16, color: Colors.grey),
        ),
      );
    }

    return Column(
      children: messages.map((message) {
        return _buildMessageCard(message);
      }).toList(),
    );
  }

  Widget _buildMessageCard(Message message) {
    final dateFormat = "${message.createdAt.day}/${message.createdAt.month}/${message.createdAt.year}";
    final timeFormat = "${message.createdAt.hour}:${message.createdAt.minute.toString().padLeft(2, '0')}";
    final isOwnMessage = message.authorId == 1; // TODO: Vérifier avec AuthService

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
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
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // En-tête du message (auteur, date, tag)
          Row(
            children: [
              // Badge auteur
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: message.authorId == 1
                      ? Colors.green.withOpacity(0.2)
                      : Colors.blue.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.person,
                      size: 12,
                      color: message.authorId == 1 ? Colors.green : Colors.blue,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      message.author?.name ??
                          (message.authorId == 1 ? "Vous" : "Utilisateur"),
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: message.authorId == 1 ? Colors.green : Colors.blue,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Text(
                "$dateFormat à $timeFormat",
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[500],
                ),
              ),
              const Spacer(),
              // Tag "Actif"
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Text(
                  "Actif",
                  style: TextStyle(
                    fontSize: 10,
                    color: Colors.green,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 12),

          // Contenu du message
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.grey.withOpacity(0.05),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              message.content,
              style: const TextStyle(
                fontSize: 14,
                color: Colors.black87,
                height: 1.5,
              ),
            ),
          ),

          const SizedBox(height: 12),

          // Actions (J'aime, Je n'aime pas, Éditer, Supprimer)
          Row(
            children: [
              // J'aime
              _buildActionButton(
                icon: Icons.thumb_up_outlined,
                count: message.likesCount,
                color: Colors.green,
                onTap: () async {
                  await api.likeMessage(message.id!);
                  _loadMessages();
                },
              ),
              const SizedBox(width: 16),
              // Je n'aime pas
              _buildActionButton(
                icon: Icons.thumb_down_outlined,
                count: message.dislikesCount,
                color: Colors.red,
                onTap: () async {
                  await api.dislikeMessage(message.id!);
                  _loadMessages();
                },
              ),
              const SizedBox(width: 16),
              // Éditer (seulement si c'est son propre message)
              if (isOwnMessage)
                _buildActionButton(
                  icon: Icons.edit_outlined,
                  label: "Éditer",
                  color: Colors.orange,
                  onTap: () => _editMessage(message),
                )
              else
                const SizedBox(width: 16),
              // Supprimer (seulement si c'est son propre message)
              if (isOwnMessage)
                _buildActionButton(
                  icon: Icons.delete_outlined,
                  label: "Supprimer",
                  color: Colors.red,
                  onTap: () => _deleteMessage(message),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required int count,
    required Color color,
    required VoidCallback onTap,
    String? label,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: color),
          if (count > 0) ...[
            const SizedBox(width: 4),
            Text(
              "$count",
              style: TextStyle(
                fontSize: 12,
                color: color,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
          if (label != null) ...[
            const SizedBox(width: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: color,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildPagination() {
    return Container(
      margin: const EdgeInsets.only(top: 24),
      padding: const EdgeInsets.symmetric(vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          IconButton(
            icon: const Icon(Icons.chevron_left),
            onPressed: currentPage > 1
                ? () {
                    setState(() => currentPage--);
                    _loadMessages();
                  }
                : null,
          ),
          Text(
            "Page : $currentPage / $totalPages",
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
            ),
          ),
          IconButton(
            icon: const Icon(Icons.chevron_right),
            onPressed: currentPage < totalPages
                ? () {
                    setState(() => currentPage++);
                    _loadMessages();
                  }
                : null,
          ),
        ],
      ),
    );
  }

  Future<void> _editMessage(Message message) async {
    final controller = TextEditingController(text: message.content);

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Éditer le message"),
        content: TextField(
          controller: controller,
          maxLines: 6,
          decoration: const InputDecoration(
            hintText: "Modifiez votre message...",
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text("Annuler"),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text("Enregistrer"),
          ),
        ],
      ),
    );

    if (result == true && controller.text.trim().isNotEmpty) {
      final updatedMessage = Message(
        id: message.id,
        forumId: message.forumId,
        authorId: message.authorId,
        content: controller.text.trim(),
        createdAt: message.createdAt,
        updatedAt: DateTime.now(),
        likesCount: message.likesCount,
        dislikesCount: message.dislikesCount,
      );

      final success = await api.updateMessage(updatedMessage);
      if (success) {
        _showSuccessSnackBar("Message modifié");
        _loadMessages();
      } else {
        _showErrorSnackBar("Erreur lors de la modification");
      }
    }
  }

  Future<void> _deleteMessage(Message message) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Confirmer la suppression"),
        content: const Text("Voulez-vous vraiment supprimer ce message ?"),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text("Annuler"),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text("Supprimer"),
          ),
        ],
      ),
    );

    if (confirm == true) {
      final success = await api.deleteMessage(message.id!);
      if (success) {
        _showSuccessSnackBar("Message supprimé");
        _loadMessages();
      } else {
        _showErrorSnackBar("Erreur lors de la suppression");
      }
    }
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
}
