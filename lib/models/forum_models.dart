// ============================================================================
// MODÈLES DE DONNÉES - Schéma SQL : forum, message, users
// ============================================================================

import 'package:flutter/material.dart';

// Modèle Utilisateur
class User {
  final int id;
  final String name;
  final String email;
  final String role; // ROLE_ADMIN, ROLE_USER
  final String? avatarUrl;

  User({
    required this.id,
    required this.name,
    required this.email,
    required this.role,
    this.avatarUrl,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      role: json['role'] ?? 'ROLE_USER',
      avatarUrl: json['avatar_url'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'role': role,
      'avatar_url': avatarUrl,
    };
  }

  bool get isAdmin => role == 'ROLE_ADMIN';
}

// Modèle Catégorie
class Category {
  final int? id;
  final String title;
  final String description;
  final String iconUrl;
  final int forumCount;
  final Color color;

  Category({
    this.id,
    required this.title,
    required this.description,
    required this.iconUrl,
    this.forumCount = 0,
    required this.color,
  });

  factory Category.fromJson(Map<String, dynamic> json) {
    return Category(
      id: json['id'],
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      iconUrl: json['icon_url'] ?? '',
      forumCount: json['forum_count'] ?? 0,
      color: _parseColor(json['color']),
    );
  }

  static Color _parseColor(dynamic colorValue) {
    if (colorValue is int) return Color(colorValue);
    if (colorValue is String) {
      switch (colorValue.toLowerCase()) {
        case 'green': return Colors.green;
        case 'blue': return Colors.blue;
        case 'orange': return Colors.orange;
        case 'red': return Colors.red;
        case 'purple': return Colors.purple;
        default: return Colors.grey;
      }
    }
    return Colors.grey;
  }
}

// Modèle Forum
class Forum {
  final int? id;
  final String title;
  final String description;
  final int categoryId;
  final int authorId;
  final User? author;
  final DateTime createdAt;
  final bool isActive;
  final int messageCount;
  final String? levelTag; // ex: "k1T", "k2S"
  final Category? category;

  Forum({
    this.id,
    required this.title,
    required this.description,
    required this.categoryId,
    required this.authorId,
    this.author,
    required this.createdAt,
    this.isActive = true,
    this.messageCount = 0,
    this.levelTag,
    this.category,
  });

  factory Forum.fromJson(Map<String, dynamic> json) {
    return Forum(
      id: json['id'],
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      categoryId: json['category_id'] ?? 0,
      authorId: json['author_id'] ?? 0,
      author: json['author'] != null ? User.fromJson(json['author']) : null,
      createdAt: DateTime.parse(json['created_at'] ?? DateTime.now().toIso8601String()),
      isActive: json['is_active'] ?? true,
      messageCount: json['message_count'] ?? 0,
      levelTag: json['level_tag'],
      category: json['category'] != null ? Category.fromJson(json['category']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'category_id': categoryId,
      'author_id': authorId,
      'created_at': createdAt.toIso8601String(),
      'is_active': isActive,
      'message_count': messageCount,
      'level_tag': levelTag,
    };
  }
}

// Modèle Message
class Message {
  final int? id;
  final int forumId;
  final int authorId;
  final User? author;
  final String content;
  final DateTime createdAt;
  final DateTime? updatedAt;
  final int likesCount;
  final int dislikesCount;
  final bool isDeleted;

  Message({
    this.id,
    required this.forumId,
    required this.authorId,
    this.author,
    required this.content,
    required this.createdAt,
    this.updatedAt,
    this.likesCount = 0,
    this.dislikesCount = 0,
    this.isDeleted = false,
  });

  factory Message.fromJson(Map<String, dynamic> json) {
    return Message(
      id: json['id'],
      forumId: json['forum_id'] ?? 0,
      authorId: json['author_id'] ?? 0,
      author: json['author'] != null ? User.fromJson(json['author']) : null,
      content: json['content'] ?? '',
      createdAt: DateTime.parse(json['created_at'] ?? DateTime.now().toIso8601String()),
      updatedAt: json['updated_at'] != null ? DateTime.parse(json['updated_at']) : null,
      likesCount: json['likes_count'] ?? 0,
      dislikesCount: json['dislikes_count'] ?? 0,
      isDeleted: json['is_deleted'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'forum_id': forumId,
      'author_id': authorId,
      'content': content,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt?.toIso8601String(),
      'likes_count': likesCount,
      'dislikes_count': dislikesCount,
      'is_deleted': isDeleted,
    };
  }
}

// Modèle pour les statistiques
class ForumStats {
  final int totalCategories;
  final int totalForums;
  final int totalMessages;
  final int activeForums;
  final int inactiveForums;

  ForumStats({
    required this.totalCategories,
    required this.totalForums,
    required this.totalMessages,
    required this.activeForums,
    required this.inactiveForums,
  });

  factory ForumStats.fromJson(Map<String, dynamic> json) {
    return ForumStats(
      totalCategories: json['total_categories'] ?? 0,
      totalForums: json['total_forums'] ?? 0,
      totalMessages: json['total_messages'] ?? 0,
      activeForums: json['active_forums'] ?? 0,
      inactiveForums: json['inactive_forums'] ?? 0,
    );
  }
}

// Modèle pour le clustering
class Cluster {
  final int id;
  final String name;
  final List<String> tags;
  final String description;
  final List<ThemeActivity> themes;
  final int forumCount;
  final double averageActivity;

  Cluster({
    required this.id,
    required this.name,
    required this.tags,
    required this.description,
    required this.themes,
    required this.forumCount,
    required this.averageActivity,
  });

  factory Cluster.fromJson(Map<String, dynamic> json) {
    return Cluster(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      tags: List<String>.from(json['tags'] ?? []),
      description: json['description'] ?? '',
      themes: (json['themes'] as List?)
          ?.map((t) => ThemeActivity.fromJson(t))
          .toList() ?? [],
      forumCount: json['forum_count'] ?? 0,
      averageActivity: (json['average_activity'] ?? 0).toDouble(),
    );
  }
}

class ThemeActivity {
  final String name;
  final double activityLevel; // 0.0 à 1.0

  ThemeActivity({
    required this.name,
    required this.activityLevel,
  });

  factory ThemeActivity.fromJson(Map<String, dynamic> json) {
    return ThemeActivity(
      name: json['name'] ?? '',
      activityLevel: (json['activity_level'] ?? 0).toDouble(),
    );
  }
}

// Modèle pour les prédictions
class ForumPrediction {
  final int forumId;
  final String forumTitle;
  final String trend; // "En hausse", "Stable", "En baisse"
  final double score;
  final String recommendation;

  ForumPrediction({
    required this.forumId,
    required this.forumTitle,
    required this.trend,
    required this.score,
    required this.recommendation,
  });

  factory ForumPrediction.fromJson(Map<String, dynamic> json) {
    return ForumPrediction(
      forumId: json['forum_id'] ?? 0,
      forumTitle: json['forum_title'] ?? '',
      trend: json['trend'] ?? 'Stable',
      score: (json['score'] ?? 0).toDouble(),
      recommendation: json['recommendation'] ?? '',
    );
  }
}
