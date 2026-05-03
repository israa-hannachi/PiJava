// ============================================================================
// PAGE FRONT-END : FORUM / THREAD
// Affiche le détail d'un forum avec éditeur de message
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import 'widgets/common_widgets.dart';
import 'messages_page.dart';

class ForumDetailPage extends StatefulWidget {
  final Forum forum;

  const ForumDetailPage({Key? key, required this.forum}) : super(key: key);

  @override
  _ForumDetailPageState createState() => _ForumDetailPageState();
}

class _ForumDetailPageState extends State<ForumDetailPage> {
  late ApiService api;
  final TextEditingController _messageController = TextEditingController();
  bool isTextMode = true; // Mode Texte vs Mode Tableau Blanc
  bool isSubmitting = false;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    api = ApiService();
  }

  @override
  void dispose() {
    _messageController.dispose();
    super.dispose();
  }

  Future<void> _publishMessage() async {
    final content = _messageController.text.trim();

    // Validation
    if (content.isEmpty) {
      setState(() => errorMessage = "Le message ne peut pas être vide");
      return;
    }

    setState(() {
      isSubmitting = true;
      errorMessage = null;
    });

    try {
      final message = Message(
        forumId: widget.forum.id!,
        authorId: 1, // TODO: Récupérer depuisAuthService.currentUser.id
        content: content,
        createdAt: DateTime.now(),
      );

      await api.createMessage(message);

      // Réinitialiser l'éditeur
      _messageController.clear();

      // Rafraîchir la page ou naviguer vers les messages
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Message publié avec succès"),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => errorMessage = "Erreur lors de la publication");
    } finally {
      setState(() => isSubmitting = false);
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
                        _buildForumHeader(),
                        const SizedBox(height: 24),

                        // Section "Nouveau message"
                        _buildNewMessageSection(),
                        const SizedBox(height: 24),

                        // Bouton Publier
                        _buildPublishButton(),
                        const SizedBox(height: 32),

                        // Bouton vers les messages
                        _buildViewMessagesButton(),
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
            onPressed: () {
              // Naviguer vers la catégorie
            },
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

  Widget _buildForumHeader() {
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
              Icon(Icons.forum, color: Colors.white.withOpacity(0.8), size: 32),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  widget.forum.title,
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
            widget.forum.description,
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.chat_bubble_outline, size: 14, color: Colors.white),
                    const SizedBox(width: 4),
                    Text(
                      "${widget.forum.messageCount} messages",
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.white,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 12),
              if (widget.forum.levelTag != null)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    widget.forum.levelTag!,
                    style: const TextStyle(
                      fontSize: 12,
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildNewMessageSection() {
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
          // Titre section
          const Text(
            "Nouveau message",
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
          const SizedBox(height: 16),

          // Modes Texte / Tableau Blanc
          Row(
            children: [
              _buildModeButton("Mode Texte", isTextMode, () {
                setState(() => isTextMode = true);
              }),
              const SizedBox(width: 12),
              _buildModeButton("Mode Tableau Blanc", !isTextMode, () {
                setState(() => isTextMode = false);
              }),
            ],
          ),
          const SizedBox(height: 16),

          // Éditeur de texte
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey.shade300),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Barre d'outils
                _buildRichEditorToolbar(),
                const SizedBox(height: 12),
                TextField(
                  controller: _messageController,
                  maxLines: 6,
                  decoration: const InputDecoration(
                    hintText: "Écrivez votre message ici...",
                    border: InputBorder.none,
                  ),
                  style: const TextStyle(fontSize: 14),
                ),
                // Message d'erreur
                if (errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      errorMessage!,
                      style: const TextStyle(color: Colors.red, fontSize: 12),
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildModeButton(String label, bool isActive, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isActive ? Colors.green : Colors.grey[200],
          borderRadius: BorderRadius.circular(8),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w500,
            color: isActive ? Colors.white : Colors.grey[700],
          ),
        ),
      ),
    );
  }

  Widget _buildRichEditorToolbar() {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        _buildToolbarButton(Icons.text_fields, "Taille"),
        _buildToolbarButton(Icons.format_bold, "B", isBold: true),
        _buildToolbarButton(Icons.format_italic, "I", isItalic: true),
        _buildToolbarButton(Icons.format_underlined, "U", isUnderline: true),
        _buildToolbarButton(Icons.format_strikethrough, "S", isStrike: true),
        _buildToolbarButton(Icons.list, "Listes"),
        _buildToolbarButton(Icons.calculate, "Σ"),
      ],
    );
  }

  Widget _buildToolbarButton(IconData icon, String tooltip,
    {bool isBold = false, bool isItalic = false, bool isUnderline = false, bool isStrike = false}) {
    return Tooltip(
      message: tooltip,
      child: IconButton(
        icon: Icon(icon, size: 18, color: Colors.grey[700]),
        onPressed: () {
          // TODO: Implémenter les actions de formatage
        },
      ),
    );
  }

  Widget _buildPublishButton() {
    return Align(
      alignment: Alignment.centerRight,
      child: ElevatedButton.icon(
        onPressed: isSubmitting ? null : _publishMessage,
        icon: isSubmitting
            ? const SizedBox(
                width: 16,
                height: 16,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: Colors.white,
                ),
              )
            : const Icon(Icons.send, size: 18),
        label: Text(isSubmitting ? "Publication..." : "Publier"),
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.green,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
      ),
    );
  }

  Widget _buildViewMessagesButton() {
    return SizedBox(
      width: double.infinity,
      child: OutlinedButton.icon(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => MessagesPage(forum: widget.forum),
            ),
          );
        },
        icon: const Icon(Icons.chat_bubble_outline),
        label: const Text("Voir tous les messages"),
        style: OutlinedButton.styleFrom(
          foregroundColor: Colors.green,
          side: const BorderSide(color: Colors.green),
          padding: const EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
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
