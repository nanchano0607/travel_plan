import axios from "axios";

const BASE_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: BASE_URL,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
if (token) {
    config.headers.Authorization = `Bearer ${token}`;
}
    return config;
});

/* ----------------- 댓글 CRUD -------------------- */

// 댓글 작성
export const createComment = (data) => api.post("/comment", data).then((res) => res.data);

// 최상위 댓글만 조회 (대댓글 제외)
export const getTopLevelComments = (postId) => api.get(`/comment/post/${postId}/top`).then((res) => res.data);

// 대댓글 조회
export const getReplies = (parentCommentId) => api.get(`/comment/${parentCommentId}/replies`).then((res) => res.data);

// 댓글 수정
export const updateComment = (commentId, content) => api.put(`/comment/${commentId}`, content, {
    headers: { "Content-Type": "application/json" },
}).then((res) => res.data);

// 댓글 삭제
export const deleteComment = (commentId) => api.delete(`/comment/${commentId}`).then((res) => res.data);

// 게시글 댓글 수 조회
export const getCommentCount = (postId) => api.get(`/comment/post/${postId}/count`).then((res) => res.data);

/* ------------------ 좋아요 -------------------- */

// 좋아요
export const addCommentLike = (commentId, userId) => api.post(`/comment/${commentId}/likes/${userId}`).then((res) => res.data);

// 좋아요 취소
export const removeCommentLike = (commentId, userId) => api.delete(`/comment/${commentId}/likes/${userId}`).then((res) => res.data);

// 좋아요 수 조회
export const getCommentLikeCount = (commentId) => api.get(`/comment/${commentId}/likes/count`).then((res) => res.data);

// 좋아요 여부 확인
export const checkCommentLiked = (commentId, userId) => api.get(`/comment/${commentId}/likes/${userId}`).then((res) => res.data);

/* ------------------ 이미지 -------------------- */

// 이미지 업로드
export const uploadCommentImage = (commentId, file) => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post(`/comment/${commentId}/images`, formData, {
        headers: {"Content-Type": "multipart/form-data"},
    })
    .then((res) => res.data);
};

// 댓글 이미지 전체 조회
export const getCommentImages = (commentId) => api.get(`/comment/${commentId}/images`).then((res) => res.data);

// 댓글 이미지 삭제
export const deleteCommentImage = (commentId, imageId) => api.delete(`/comment/${commentId}/images/${imageId}`).then((res) => res.data);

export default api